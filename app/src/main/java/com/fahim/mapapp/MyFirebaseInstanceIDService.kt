package com.fahim.mapapp

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fahim.mapapp.BaseApplication.Companion.CHANNEL_ID
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives FCM registration token updates and incoming push messages.
 *
 * Modern replacement for the removed `FirebaseInstanceIdService.onTokenRefresh()` /
 * `FirebaseInstanceId.getToken()` APIs: Firebase now calls [onNewToken] whenever the
 * token is created or rotated, and [FirebaseMessaging.getToken] fetches the current one.
 *
 * firebase-messaging 25.x deprecates these in favour of `register()` / `onRegistered()`;
 * they still work, so the deprecation warnings are suppressed here.
 */
@Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
class MyFirebaseInstanceIDService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseIIDService"
        private const val DEFAULT_TITLE = "MapApp"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            Log.d(TAG, "Refreshed token: ${task.result}")
        }
    }

    /**
     * Called for data messages, and for notification messages while the app is in the
     * foreground. (In the background, the system tray shows notification messages itself.)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message from: ${message.from}, data: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: DEFAULT_TITLE
        val body = message.notification?.body ?: message.data["body"] ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        if (!canPostNotifications()) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted; dropping message: $title")
            return
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission revoked while posting", e)
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}
