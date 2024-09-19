package com.fahim.mapapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fahim.mapapp.MainActivity;

public class LocationBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("LOCATION_UPDATE")) {
            ((MainActivity) context).disableStartButton();
        }
    }
}