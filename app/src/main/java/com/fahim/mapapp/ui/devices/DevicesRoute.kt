package com.fahim.mapapp.ui.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fahim.mapapp.data.Device
import com.fahim.mapapp.data.DeviceRepository
import com.fahim.mapapp.data.LoadState
import com.fahim.mapapp.ui.map.FOLLOW_ZOOM
import com.fahim.mapapp.ui.map.TelemetryMap
import com.fahim.mapapp.ui.map.toLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private const val FIT_PADDING_PX = 120
private const val TAG = "DevicesRoute"

/** Devices tab wired to Firebase: live device list plus a map of all located devices. */
@Composable
fun DevicesRoute(
    repository: DeviceRepository,
    onMonitor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val flow = remember(repository) { repository.observeDevices() }
    val devices by flow.collectAsStateWithLifecycle(initialValue = LoadState.Loading)
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberCameraPositionState()

    val located = (devices as? LoadState.Ready)?.data.orEmpty().filter { it.isLocated }
    val selected = located.firstOrNull { it.id == selectedId }

    LaunchedEffect(selected?.location, located.size) {
        when {
            selected?.location != null ->
                camera.animate(CameraUpdateFactory.newLatLngZoom(selected.location.toLatLng(), FOLLOW_ZOOM))
            located.size == 1 ->
                camera.animate(CameraUpdateFactory.newLatLngZoom(located.single().requireLatLng(), FOLLOW_ZOOM - 2))
            located.size > 1 -> {
                val bounds = LatLngBounds.builder().apply { located.forEach { include(it.requireLatLng()) } }.build()
                try {
                    camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, FIT_PADDING_PX))
                } catch (e: IllegalStateException) {
                    // Map not laid out yet; the next device update will retry the fit.
                    Log.w(TAG, "Skipping bounds fit: ${e.message}")
                }
            }
        }
    }

    DevicesScreen(
        devices = devices,
        query = query,
        selectedId = selectedId,
        onQueryChange = { query = it },
        onSelect = { selectedId = it },
        onConfirm = onMonitor,
        modifier = modifier
    ) {
        TelemetryMap(cameraPositionState = camera) {
            located.forEach { device ->
                key(device.id) {
                    DeviceMarker(device, selected = device.id == selectedId, onClick = { selectedId = device.id })
                }
            }
        }
    }
}

/** Marker whose position follows the device without being re-created on each update. */
@Composable
@GoogleMapComposable
private fun DeviceMarker(device: Device, selected: Boolean, onClick: () -> Unit) {
    val position = device.requireLatLng()
    val markerState = rememberMarkerState(position = position)
    LaunchedEffect(position) { markerState.position = position }
    Marker(
        state = markerState,
        title = device.id,
        icon = BitmapDescriptorFactory.defaultMarker(
            if (selected) BitmapDescriptorFactory.HUE_VIOLET else BitmapDescriptorFactory.HUE_AZURE
        ),
        onClick = {
            onClick()
            false
        }
    )
}

private fun Device.requireLatLng() = requireNotNull(location) { "Device $id has no location" }.toLatLng()
