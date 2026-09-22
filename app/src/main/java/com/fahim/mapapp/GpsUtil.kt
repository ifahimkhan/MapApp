package com.fahim.mapapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GpsUtil {
    private  FusedLocationProviderClient fusedLocationProviderClient;
    private  LocationRequest locationRequest;

    public static boolean hasLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public  void getLocation(Context context, LocationCallback locationCallback) {

        if (!hasLocationPermissions(context)) {
            Log.e("TAG", "Missing location permissions.");
            return;
        }
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setIntervalMillis(10000)
                    .build();
            locationUpdates(context, locationCallback);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "error: " + e.getMessage());
        }
    }

    private  void locationUpdates(Context context, LocationCallback locationCallback) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }
            } else {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }
    public void closeLocationUpdates(LocationCallback locationCallback){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        fusedLocationProviderClient.flushLocations();
    }

}
