package com.fahim.mapapp.service;

import static com.fahim.mapapp.BaseApplication.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.fahim.mapapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String deviceId;

    public static void getInstance(Context context, String deviceId) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.putExtra("deviceId", deviceId);
        context.startService(serviceIntent);

    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("devices");
    Map<String, Object> locationData = new HashMap<>();
    Map<String, Object> deviceLocation = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setIntervalMillis(10000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.e("TAG", "service:onLocationResult " + location.getProvider() + location.getLatitude() + "-" + location.getLongitude());
                    createNotification(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                    updateToFirebase(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                    sendBroadcast(new Intent("LOCATION_UPDATE"));
                }
            }
        };
    }

    private void updateToFirebase(String latitude, String longitude) {

        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        deviceLocation.put(deviceId, locationData);
        myRef.updateChildren(deviceLocation);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP_SERVICE".equals(intent.getAction())) {
            stopSelf(); // Stop the service
            return START_NOT_STICKY;
        } else {
            deviceId = intent.getStringExtra("deviceId");
            locationUpdates();
            return START_STICKY;
        }
    }

    private void locationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }
            } else {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void createNotification(String lat, String lng) {
        Intent stopServiceIntent = new Intent(this, LocationService.class);
        stopServiceIntent.setAction("STOP_SERVICE");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_notification_clear_all,
                "Stop Service",
                stopPendingIntent)
                .build();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("Latitude and Longitude")
                .setContentText(lat + " - " + lng)
                .addAction(stopAction)
                .build();

        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "service destroyed: ");
        stopForeground(STOP_FOREGROUND_REMOVE);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}