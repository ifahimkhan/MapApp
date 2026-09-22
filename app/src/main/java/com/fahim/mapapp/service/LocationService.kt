package com.fahim.mapapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fahim.mapapp.BaseApplication.Companion.CHANNEL_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LocationService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var deviceId: String? = null

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("devices")
    private val locationData: MutableMap<String, Any> = HashMap()
    private val deviceLocation: MutableMap<String, Any> = HashMap()

    companion object {
        const val ACTION_LOCATION_UPDATE = "LOCATION_UPDATE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val EXTRA_DEVICE_ID = "deviceId"

        @JvmStatic
        fun getInstance(context: Context, deviceId: String) {
            val serviceIntent = Intent(context, LocationService::class.java).apply {
                putExtra(EXTRA_DEVICE_ID, deviceId)
            }
            context.startService(serviceIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setIntervalMillis(10000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    Log.e(
                        "TAG",
                        "service:onLocationResult ${location.provider}${location.latitude}-${location.longitude}"
                    )
                    createNotification(location.latitude.toString(), location.longitude.toString())
                    updateToFirebase(location.latitude.toString(), location.longitude.toString())
                    sendBroadcast(Intent(ACTION_LOCATION_UPDATE))
                }
            }
        }
    }

    private fun updateToFirebase(latitude: String, longitude: String) {
        val id = deviceId ?: return
        locationData["latitude"] = latitude
        locationData["longitude"] = longitude
        deviceLocation[id] = locationData
        myRef.updateChildren(deviceLocation)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_STOP_SERVICE == intent.action) {
            stopSelf()
            return START_NOT_STICKY
        } else {
            deviceId = intent?.getStringExtra(EXTRA_DEVICE_ID)
            locationUpdates()
            return START_STICKY
        }
    }

    private fun locationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            } else {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createNotification(lat: String, lng: String) {
        val stopServiceIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_notification_clear_all,
            "Stop Service",
            stopPendingIntent
        ).build()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Latitude and Longitude")
            .setContentText("$lat - $lng")
            .addAction(stopAction)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.e("TAG", "service destroyed: ")
        stopForeground(STOP_FOREGROUND_REMOVE)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
