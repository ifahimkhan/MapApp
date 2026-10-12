package com.fahim.mapapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fahim.mapapp.data.DeviceLocation
import com.fahim.mapapp.data.DeviceRepository
import com.fahim.mapapp.ui.session.LiveSessionRoute
import com.fahim.mapapp.ui.theme.MapAppTheme
import java.util.Locale

/** Hosts the live session for one device. */
class MapsActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SELECTED_DEVICE_ID = "selectedDeviceId"
        private const val TAG = "MapsActivity"

        @JvmStatic
        fun getInstance(context: Context, selectedDeviceId: String) {
            val intent = Intent(context, MapsActivity::class.java).apply {
                putExtra(EXTRA_SELECTED_DEVICE_ID, selectedDeviceId)
            }
            context.startActivity(intent)
        }
    }

    private val repository by lazy { DeviceRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val deviceId = intent.getStringExtra(EXTRA_SELECTED_DEVICE_ID)?.trim().orEmpty()
        if (deviceId.isEmpty()) {
            Log.w(TAG, "Started without a device ID; closing")
            Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MapAppTheme {
                LiveSessionRoute(
                    deviceId = deviceId,
                    repository = repository,
                    onBack = ::finish,
                    onShare = { shareLocation(deviceId, it) },
                    onOpenInMaps = ::openInMaps
                )
            }
        }
    }

    private fun mapsUrl(location: DeviceLocation): String =
        String.format(Locale.US, "https://maps.google.com/?q=%.6f,%.6f", location.latitude, location.longitude)

    private fun shareLocation(deviceId: String, location: DeviceLocation) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$deviceId is here: ${mapsUrl(location)}")
        }
        startActivity(Intent.createChooser(send, "Share location"))
    }

    private fun openInMaps(location: DeviceLocation) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl(location))))
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No app can open map links", e)
            Toast.makeText(this, "No app available to open maps", Toast.LENGTH_SHORT).show()
        }
    }
}
