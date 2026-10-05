package com.fahim.mapapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GpsUtil {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    companion object {
        @JvmStatic
        fun hasLocationPermissions(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context, locationCallback: LocationCallback) {
        if (!hasLocationPermissions(context)) {
            Log.e("TAG", "Missing location permissions.")
            return
        }
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setIntervalMillis(10000)
                .build()
            locationUpdates(context, locationCallback)
        } catch (e: Exception) {
            Log.e("TAG", "error: ${e.message}", e)
        }
    }

    private fun locationUpdates(context: Context, locationCallback: LocationCallback) {
        val request = locationRequest ?: return
        val client = fusedLocationProviderClient ?: return

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
                }
            } else {
                client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            }
        }
    }

    fun closeLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationProviderClient?.let {
            it.removeLocationUpdates(locationCallback)
            it.flushLocations()
        }
    }
}
