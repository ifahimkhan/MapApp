package com.fahim.mapapp;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class GpsUtil {

    public static boolean hasLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @SuppressLint("MissingPermission")
    public static Location getLocation(Context context, LocationListener listener) {

        if (!hasLocationPermissions(context)) {
            Log.e("TAG", "Missing location permissions.");
            return null;
        }
        try {
            LocationManager locationManager =
                    (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);

            String provider = getWorkingProvider(locationManager);
            Log.e("TAG", "provider: "+provider);
            if (provider == null) {
                Log.e("TAG", "No location provider available.");
                return null;
            }
            if (provider.equals(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates
                        (provider, 30000,
                                50.0f, listener);
            }else if (provider.equals(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates
                        (provider, 3000,
                                5.0f, listener);
            }


            return locationManager.getLastKnownLocation(provider);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "error: " + e.getMessage());
            return null;
        }
    }

    private static String getWorkingProvider(LocationManager locationManager) {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return LocationManager.GPS_PROVIDER;
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            return LocationManager.NETWORK_PROVIDER;

        else return null;
    }


}
