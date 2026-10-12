package com.fahim.mapapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.fahim.mapapp.data.DeviceRepository
import com.fahim.mapapp.service.LocationService
import com.fahim.mapapp.ui.common.rememberLocationPermissionGate
import com.fahim.mapapp.ui.devices.DevicesRoute
import com.fahim.mapapp.ui.home.HomeScreen
import com.fahim.mapapp.ui.home.HomeTab
import com.fahim.mapapp.ui.theme.MapAppTheme
import com.fahim.mapapp.ui.tracker.DeviceIdDialog
import com.fahim.mapapp.ui.tracker.TrackerRoute

class MainActivity : ComponentActivity() {

    private val tag = MainActivity::class.java.simpleName
    private val repository by lazy { DeviceRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MapAppTheme {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        var tab by rememberSaveable { mutableStateOf(HomeTab.Tracker) }
        var trackingDeviceId by rememberSaveable { mutableStateOf<String?>(null) }
        var showDeviceIdDialog by rememberSaveable { mutableStateOf(false) }
        val withLocationPermission = rememberLocationPermissionGate()

        HomeScreen(selectedTab = tab, onTabSelected = { tab = it }) { current ->
            when (current) {
                HomeTab.Tracker -> TrackerRoute(
                    trackingDeviceId = trackingDeviceId,
                    repository = repository,
                    onStartTracking = { withLocationPermission { showDeviceIdDialog = true } },
                    onStopTracking = {
                        stopTracking()
                        trackingDeviceId = null
                    },
                    onOpenSession = ::openSession,
                    onBrowseDevices = { tab = HomeTab.Devices }
                )
                HomeTab.Devices -> DevicesRoute(
                    repository = repository,
                    onMonitor = { id -> withLocationPermission { openSession(id) } }
                )
            }
        }

        if (showDeviceIdDialog) {
            DeviceIdDialog(
                onDismiss = { showDeviceIdDialog = false },
                onConfirm = { deviceId ->
                    showDeviceIdDialog = false
                    startTracking(deviceId)
                    trackingDeviceId = deviceId
                }
            )
        }
    }

    private fun startTracking(deviceId: String) {
        Log.i(tag, "Starting location broadcast as $deviceId")
        LocationService.getInstance(this, deviceId)
    }

    private fun stopTracking() {
        Log.i(tag, "Stopping location broadcast")
        startService(Intent(this, LocationService::class.java).setAction(LocationService.ACTION_STOP_SERVICE))
    }

    private fun openSession(deviceId: String) {
        MapsActivity.getInstance(this, deviceId)
    }
}
