package com.fahim.mapapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class BaseApplication : Application() {

    companion object {
        const val CHANNEL_ID = "channel_id"
        const val CHANNEL_NAME = "Channel Name"
        private const val FCM_TAG = "MyFirebaseIIDService"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        logFirebaseIds()
    }

    /**
     * onNewToken() only fires when a token is first created or rotated, so on an
     * already-installed app nothing is logged. Log the current IDs on every launch
     * to make FCM / In-App Messaging testing easy.
     */
    private fun logFirebaseIds() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(FCM_TAG, "Current FCM token: ${task.result}")
            } else {
                Log.w(FCM_TAG, "Fetching FCM token failed", task.exception)
            }
        }
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(FCM_TAG, "Firebase Installation ID (FIAM test device): ${task.result}")
            } else {
                Log.w(FCM_TAG, "Fetching Installation ID failed", task.exception)
            }
        }
    }
}
