# Testing FCM & Firebase In-App Messaging

Guide for verifying push notifications (FCM) and In-App Messaging (FIAM) in MapApp.

---

## 1. Why `MyFirebaseIIDService` didn't show in Logcat

`MyFirebaseInstanceIDService.onNewToken()` is **only called when a token is created or rotated**:

- first launch after a fresh install
- after **Clear data** / reinstall
- when Firebase rotates the token (rare)

If the app was already installed (token already exists), `onNewToken()` never runs, so nothing is logged.
`onMessageReceived()` only logs when a message actually arrives.

**Fix applied:** `BaseApplication.logFirebaseIds()` now logs the current FCM token and the
Firebase Installation ID on **every app launch** under the same tag `MyFirebaseIIDService`.

Other common reasons for no logs:

| Problem | Check |
|---|---|
| Logcat filter wrong | Use `tag:MyFirebaseIIDService` and level **Debug** (or Verbose) |
| Wrong process / device selected in Logcat | Select running device + `com.fahim.mapapp` |
| Emulator without Google Play | Use an AVD image with the **Play Store** icon (Google APIs/Play) |
| `google-services.json` missing / wrong package | File at `app/google-services.json`, `package_name` = `com.fahim.mapapp` |
| No internet on device | Token fetch fails → look for `Fetching FCM token failed` warning |

---

## 2. Get the FCM token and Installation ID

1. Build and run the app.
2. Optional (to also trigger `onNewToken`): **Settings → Apps → MapApp → Storage → Clear data**, or uninstall/reinstall.
3. Android Studio → **Logcat** → filter: `tag:MyFirebaseIIDService`
4. Expected output:
   ```
   D MyFirebaseIIDService: Current FCM token: fXyz...:APA91b...
   D MyFirebaseIIDService: Firebase Installation ID (FIAM test device): cAbC123...
   D MyFirebaseIIDService: Refreshed token: fXyz...   <- only on fresh install / clear data
   ```
5. Copy the **FCM token** (for push tests) and the **Installation ID** (for In-App Messaging tests).

Command-line alternative:
```bash
adb logcat -s MyFirebaseIIDService
```

---

## 3. Test FCM push notifications

### Prerequisites
- Android 13+: allow the **notification permission** when the app asks (or Settings → Apps → MapApp → Notifications → On).
  Without it, `onMessageReceived` logs `POST_NOTIFICATIONS not granted; dropping message`.

### Steps (Firebase Console)
1. Open [Firebase Console](https://console.firebase.google.com) → your project.
2. **Engage → Messaging → Create your first campaign / New campaign → Firebase Notification messages**.
3. Enter **Notification title** and **Notification text**.
4. Click **Send test message** (right side).
5. Paste the **FCM token** from Logcat → click **+** → **Test**.

### Expected behaviour

| App state | Result |
|---|---|
| **Foreground** (app open) | `onMessageReceived` runs → Logcat shows `Message from: ...` → app builds notification itself |
| **Background / killed** | System tray shows notification automatically; `onMessageReceived` is **not** called (no log) — this is normal |

To get `onMessageReceived` in background too, send a **data-only** message (see section 5).

---

## 4. Test In-App Messaging (FIAM)

In-App Messaging shows a banner/modal/card **inside the app while it is open**. It is triggered by
Analytics events (default trigger: `app_open` / `on_foreground`).

### Steps
1. Get the **Installation ID** from Logcat (section 2).
   FIAM also logs it itself:
   ```bash
   adb logcat -s FIAM.Headless
   # I FIAM.Headless: Starting InAppMessaging runtime with Installation ID cAbC123...
   ```
2. Firebase Console → **Engage → In-App Messaging → Create campaign** (or *New campaign*).
3. Choose a layout (Card / Modal / Image only / Top banner), add title, body, button.
4. Click **Test on device** → paste the **Installation ID** → **+** → **Test**.
5. On the device: put the app in **background**, then **reopen it** (foreground event).
   The test message should appear.

### Real campaign (not test)
- Fill **Target** (app `com.fahim.mapapp`), **Scheduling** (start now), **Conversion events** (optional) → **Publish**.
- Campaigns are fetched **once per day** per device; to see a new campaign quickly:
  **Clear data** / reinstall, or use **Test on device** instead.
- Trigger by default is **On app foreground**; you can also choose a custom Analytics event.

### Troubleshooting FIAM
| Symptom | Fix |
|---|---|
| Nothing shows | Background → foreground the app (it doesn't show on the exact same session) |
| Still nothing | Check `adb logcat -s FIAM.Headless FIAM.Display` for errors |
| Published campaign not shown | Wait for daily fetch, or clear data, or use *Test on device* |
| Analytics not logging | Enable debug: `adb shell setprop debug.firebase.analytics.app com.fahim.mapapp` then check **Analytics → DebugView** |

---

## 5. Optional: send FCM via HTTP v1 API (advanced)

Useful for **data-only** messages (always delivered to `onMessageReceived`, even in background).

1. Firebase Console → **Project settings → Service accounts → Generate new private key** (keep it secret, never commit it).
2. Get an access token (requires Google Cloud SDK):
   ```bash
   gcloud auth application-default login
   gcloud auth application-default print-access-token
   ```
3. Send:
   ```bash
   curl -X POST \
     -H "Authorization: Bearer <ACCESS_TOKEN>" \
     -H "Content-Type: application/json" \
     https://fcm.googleapis.com/v1/projects/<PROJECT_ID>/messages:send \
     -d '{
       "message": {
         "token": "<FCM_TOKEN>",
         "data": { "title": "Hello", "body": "Data message from curl" }
       }
     }'
   ```
4. Logcat should show `Message from: ...` and a notification built by `MyFirebaseInstanceIDService`.

---

## 6. Quick checklist

- [ ] `app/google-services.json` present (ignored by git — do not commit)
- [ ] Device/emulator has Google Play services + internet
- [ ] Notification permission granted (Android 13+)
- [ ] Logcat shows `Current FCM token` and `Installation ID`
- [ ] FCM test message received (foreground + background)
- [ ] FIAM test message shown after background → foreground
