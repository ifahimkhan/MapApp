# MapApp — Location Tracker

Android app that shares a device's live GPS location through Firebase Realtime Database and shows any broadcasting device on a Google Map. Built with Kotlin and Jetpack Compose.

## Features

- **Start tracking** — enter a device ID; a foreground service pushes the device's location to Firebase every ~10 seconds.
- **Track a device** — pick any device currently broadcasting and watch its marker move on the map in real time.
- **Ongoing notification** — shows the latest latitude/longitude, with a **Stop** action to end tracking.
- **Push notifications (FCM)** — receives Firebase Cloud Messaging notification and data messages; shows them as system notifications.
- **In-App Messaging** — Firebase In-App Messaging campaigns (banner, modal, card, image) shown while the app is open.
- **Material 3 UI** — Compose screens with light/dark theme support.

## Tech Stack

| Area | Library |
|------|---------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (BOM 2024.06.00), Material 3 |
| Maps | Google Maps SDK + `maps-compose` 4.4.1 |
| Location | Play Services Location (`FusedLocationProviderClient`) |
| Backend | Firebase Realtime Database |
| Messaging | Firebase Cloud Messaging 25.1.3, In-App Messaging Display 22.0.3, Analytics 23.0.0 |
| Build | AGP 8.5, Gradle version catalog, Secrets Gradle Plugin |

Min SDK 26 · Target/Compile SDK 34

## Project Structure

```
app/src/main/java/com/fahim/mapapp/
├── BaseApplication.kt          # Notification channel; logs FCM token + Installation ID on launch
├── MyFirebaseInstanceIDService.kt # FCM service: onNewToken + onMessageReceived → notification
├── MainActivity.kt             # Home screen, device ID dialog, device picker
├── MapsActivity.kt             # Google Map showing selected device's live location
├── GpsUtil.kt                  # Location settings / GPS enable helper
├── service/
│   ├── LocationService.kt      # Foreground service: location updates → Firebase + notification
│   └── LocationBroadCastReceiver.kt
└── ui/theme/                   # Compose colors and theme
```

## How It Works

1. User taps **Start Tracking** and enters a device ID.
2. `LocationService` runs as a foreground service (`foregroundServiceType="location"`), requesting high-accuracy updates every 10 seconds.
3. Each update is written to Firebase:
   ```
   devices/
     └── <deviceId>/
           ├── latitude:  25.2048
           └── longitude: 55.2708
   ```
4. **Track Location in Map!** lists every device under `devices/`. Selecting one opens `MapsActivity`, which listens to that node and moves the marker as values change.

### Push notifications & In-App Messaging

- `MyFirebaseInstanceIDService` (a `FirebaseMessagingService`) logs new tokens in `onNewToken()` and turns incoming messages into notifications in `onMessageReceived()`.
- In foreground, all messages go through `onMessageReceived()`. In background, notification messages are shown by the system tray; data-only messages still reach `onMessageReceived()`.
- In-App Messaging needs no code — the `firebase-inappmessaging-display` dependency shows campaigns automatically, triggered by Analytics events (default: app foreground).

## Setup

### 1. Google Maps API key

1. In [Google Cloud Console](https://console.cloud.google.com/), enable **Maps SDK for Android** and create an API key (restrict it to package `com.fahim.mapapp`).
2. Add it to `local.properties` in the project root (this file is git-ignored):
   ```properties
   MAPS_API_KEY=AIza...
   ```
   The Secrets Gradle Plugin injects it into `AndroidManifest.xml` as `${MAPS_API_KEY}`.

### 2. Firebase

1. Create a project in the [Firebase Console](https://console.firebase.google.com/) and register an Android app with package `com.fahim.mapapp`.
2. Download `google-services.json` and place it in `app/`.
3. Enable **Realtime Database**.
4. Cloud Messaging and In-App Messaging need no extra console setup — create campaigns under **Engage → Messaging / In-App Messaging**.

> Do not commit `google-services.json` or API keys to a public repository.

### 3. Build and run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Or open the project in Android Studio and press **Run**.

## Testing FCM & In-App Messaging

On each launch the app logs IDs under the `MyFirebaseIIDService` tag:

```bash
adb logcat -s MyFirebaseIIDService
# D MyFirebaseIIDService: Current FCM token: ...
# D MyFirebaseIIDService: Firebase Installation ID (FIAM test device): ...
```

- **FCM:** Firebase Console → Messaging → New campaign → **Send test message** → paste the FCM token.
- **In-App Messaging:** Firebase Console → In-App Messaging → Create campaign → **Test on device** → paste the Installation ID, then background and reopen the app.

> `onNewToken()` only fires on first install, after clearing app data, or on token rotation — that's why the launch-time log exists.

Full step-by-step guide and troubleshooting: [docs/FCM_AND_IN_APP_MESSAGING_TESTING.md](docs/FCM_AND_IN_APP_MESSAGING_TESTING.md).

## Permissions

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Read device location |
| `ACCESS_BACKGROUND_LOCATION` | Keep tracking while app is in background |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_LOCATION` | Run the location service |
| `POST_NOTIFICATIONS` | Show tracking and push notifications (Android 13+) |
| `INTERNET` | Sync with Firebase, receive FCM/In-App messages, load map tiles |

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Author

[Fahim Khan](https://github.com/ifahimkhan)
