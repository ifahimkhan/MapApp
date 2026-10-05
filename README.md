# MapApp — Location Tracker

Android app that shares a device's live GPS location through Firebase Realtime Database and shows any broadcasting device on a Google Map. Built with Kotlin and Jetpack Compose.

## Features

- **Start tracking** — enter a device ID; a foreground service pushes the device's location to Firebase every ~10 seconds.
- **Track a device** — pick any device currently broadcasting and watch its marker move on the map in real time.
- **Ongoing notification** — shows the latest latitude/longitude, with a **Stop** action to end tracking.
- **Material 3 UI** — Compose screens with light/dark theme support.

## Tech Stack

| Area | Library |
|------|---------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (BOM 2024.06.00), Material 3 |
| Maps | Google Maps SDK + `maps-compose` 4.4.1 |
| Location | Play Services Location (`FusedLocationProviderClient`) |
| Backend | Firebase Realtime Database |
| Build | AGP 8.5, Gradle version catalog, Secrets Gradle Plugin |

Min SDK 26 · Target/Compile SDK 34

## Project Structure

```
app/src/main/java/com/fahim/mapapp/
├── BaseApplication.kt          # Creates the notification channel
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

> Do not commit `google-services.json` or API keys to a public repository.

### 3. Build and run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Or open the project in Android Studio and press **Run**.

## Permissions

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Read device location |
| `ACCESS_BACKGROUND_LOCATION` | Keep tracking while app is in background |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_LOCATION` | Run the location service |
| `POST_NOTIFICATIONS` | Show tracking notification (Android 13+) |
| `INTERNET` | Sync with Firebase and load map tiles |

## Author

[Fahim Khan](https://github.com/ifahimkhan)
