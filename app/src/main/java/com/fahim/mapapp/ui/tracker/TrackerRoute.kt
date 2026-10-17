package com.fahim.mapapp.ui.tracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fahim.mapapp.data.DeviceLocation
import com.fahim.mapapp.data.DeviceRepository
import com.fahim.mapapp.data.LoadState
import com.fahim.mapapp.ui.common.rememberNowMillis
import com.fahim.mapapp.ui.map.FOLLOW_ZOOM
import com.fahim.mapapp.ui.map.TelemetryMap
import com.fahim.mapapp.ui.map.toLatLng
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.rememberCameraPositionState

private const val BEACON_RADIUS_METERS = 40.0

/** Tracker tab wired to Firebase: follows this phone's own published location. */
@Composable
fun TrackerRoute(
    trackingDeviceId: String?,
    repository: DeviceRepository,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onOpenSession: (String) -> Unit,
    onBrowseDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    var location by remember(trackingDeviceId) { mutableStateOf<DeviceLocation?>(null) }
    var lastUpdate by remember(trackingDeviceId) { mutableStateOf<Long?>(null) }
    val now by rememberNowMillis()
    val camera = rememberCameraPositionState()

    LaunchedEffect(trackingDeviceId) {
        val id = trackingDeviceId ?: return@LaunchedEffect
        repository.observeLocation(id).collect { state ->
            val fix = (state as? LoadState.Ready)?.data ?: return@collect
            location = fix
            lastUpdate = System.currentTimeMillis()
            camera.animate(CameraUpdateFactory.newLatLngZoom(fix.toLatLng(), FOLLOW_ZOOM))
        }
    }

    TrackerScreen(
        state = TrackerUiState(trackingDeviceId, location, lastUpdate),
        nowMillis = now,
        onStartTracking = onStartTracking,
        onStopTracking = onStopTracking,
        onOpenSession = { trackingDeviceId?.let(onOpenSession) },
        onBrowseDevices = onBrowseDevices,
        modifier = modifier
    ) {
        TelemetryMap(cameraPositionState = camera) {
            location?.let {
                Circle(
                    center = it.toLatLng(),
                    radius = BEACON_RADIUS_METERS,
                    fillColor = ElectricIndigo.copy(alpha = 0.25f),
                    strokeColor = ElectricIndigo,
                    strokeWidth = 4f
                )
            }
        }
    }
}
