Location Tracker App

This project demonstrates a basic Android application that tracks a user's location, integrates with Firebase, and provides notifications using a BroadcastReceiver. It includes Google Maps integration using the Google Cloud Platform API.

Features
Real-time location tracking.
Google Maps integration.
Firebase integration.
Push notifications using BroadcastReceiver.
Background services to track location updates.
Step-by-Step Implementation Guide
1. Add Dependency in Gradle
Open your build.gradle (Module) file and add the following dependencies:

Sync your project after adding the dependencies.

2. Design Main Activity
In your MainActivity.java, design the UI to include a button to launch the MapsActivity. You can create a simple layout with a button using XML:


<Button
    android:id="@+id/btnStartTracking"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Start Tracking" />
Set up an OnClickListener in MainActivity to start the MapsActivity.

3. Create MapsActivity from Template
Create a new MapsActivity from the Google Maps Activity template. To do this:

Right-click on your package in Android Studio.
Select New -> Google -> Google Maps Activity.
This will automatically generate MapsActivity.java and activity_maps.xml, along with the necessary configuration in AndroidManifest.xml.
4. Create Location Service
To track the user’s location, create a background service called LocationService:

Create a new class LocationService.java.
Use FusedLocationProviderClient to fetch the current location.

5. Connect Account with Firebase
Follow these steps to connect Firebase to your project:

Go to the Firebase Console.
Create a new Firebase project and register your Android app.
Download the google-services.json file and place it in the app/ directory.
Add the Firebase SDK by modifying your project-level build.gradle:

6. Create Google Cloud Platform Project for API Key
Go to the Google Cloud Console.
Create a new project and enable the Maps SDK for Android.
Generate an API key and restrict it to your app’s package name.
Add the API key to your AndroidManifest.xml:

7. Create Broadcast Receiver and Application-Level Class for Notifications
Create a BroadcastReceiver to handle notifications:


To set up application-wide notification handling, create an Application class:

In MyApplication.java:

Update the AndroidManifest.xml to declare the Application and BroadcastReceiver.


<application
    android:name=".MyApplication">
    <receiver android:name=".NotificationReceiver"/>
</application>
8. Add Required Permissions in Manifest
Ensure you have the following permissions in your AndroidManifest.xml for location tracking and internet access:


<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
If location services are required in the background, include:
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
License
This project is licensed under the MIT License - see the LICENSE file for details.
