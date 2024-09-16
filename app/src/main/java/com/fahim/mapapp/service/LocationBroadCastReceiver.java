package com.fahim.mapapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

public class LocationBroadCastReceiver extends BroadcastReceiver {

    LocationCallback locationCallback;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("LOCATION_UPDATE")) {
            LocationResult result = intent.getParcelableExtra("result");
            double latitude = result.getLastLocation().getLatitude();
            double longitude = result.getLastLocation().getLongitude();
            Log.e("TAG", "LOCATION_UPDATE: " + latitude + "-" + longitude);
            if (locationCallback != null && result != null) {
                locationCallback.onLocationResult(result);
            }
        }
    }

    public void setCallBack(LocationCallback locationCallback) {
        this.locationCallback = locationCallback;
    }
}