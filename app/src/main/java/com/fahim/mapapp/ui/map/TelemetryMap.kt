package com.fahim.mapapp.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.fahim.mapapp.R
import com.fahim.mapapp.data.DeviceLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

/** Default zoom for following a single device. */
const val FOLLOW_ZOOM = 16f

fun DeviceLocation.toLatLng(): LatLng = LatLng(latitude, longitude)

fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

/**
 * GoogleMap with the Obsidian night style and the stock chrome turned off,
 * since screens draw their own glass controls.
 */
@Composable
fun TelemetryMap(
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
    content: @Composable @GoogleMapComposable () -> Unit = {}
) {
    val context = LocalContext.current
    val style = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_obsidian) }
    val showMyLocation = remember { context.hasLocationPermission() }
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = style, isMyLocationEnabled = showMyLocation),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        ),
        content = content
    )
}
