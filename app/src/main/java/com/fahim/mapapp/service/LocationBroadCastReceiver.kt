package com.fahim.mapapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fahim.mapapp.MainActivity

class LocationBroadCastReceiver(
    private val onLocationUpdated: (() -> Unit)? = null
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "LOCATION_UPDATE") {
            onLocationUpdated?.invoke()
            if (context is MainActivity) {
                context.disableStartButton()
            }
        }
    }
}
