package com.fahim.mapapp.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fahim.mapapp.data.DeviceLocation
import com.fahim.mapapp.data.DeviceRepository
import com.fahim.mapapp.data.Fix
import com.fahim.mapapp.data.LoadState
import com.fahim.mapapp.data.appendFix
import com.fahim.mapapp.ui.common.rememberNowMillis
import com.fahim.mapapp.ui.map.FOLLOW_ZOOM
import com.fahim.mapapp.ui.map.TelemetryMap
import com.fahim.mapapp.ui.map.toLatLng
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.LuminousViolet
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private const val TRAIL_WIDTH_PX = 10f
private const val BEACON_RADIUS_METERS = 30.0

/** Live session wired to Firebase: collects fixes for [deviceId] and draws the trail. */
@Composable
fun LiveSessionRoute(
    deviceId: String,
    repository: DeviceRepository,
    onBack: () -> Unit,
    onShare: (DeviceLocation) -> Unit,
    onOpenInMaps: (DeviceLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionStart = rememberSaveable { System.currentTimeMillis() }
    var location by remember { mutableStateOf<LoadState<DeviceLocation?>>(LoadState.Loading) }
    var fixes by remember { mutableStateOf(emptyList<Fix>()) }
    val now by rememberNowMillis()
    val camera = rememberCameraPositionState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(deviceId) {
        repository.observeLocation(deviceId).collect { state ->
            location = state
            val fix = (state as? LoadState.Ready)?.data ?: return@collect
            fixes = appendFix(fixes, Fix(fix, System.currentTimeMillis()))
            camera.animate(CameraUpdateFactory.newLatLngZoom(fix.toLatLng(), FOLLOW_ZOOM))
        }
    }

    fun moveCamera(update: CameraUpdate) {
        scope.launch { camera.animate(update) }
    }

    LiveSessionScreen(
        state = LiveSessionUiState(deviceId, location, fixes, sessionStart),
        nowMillis = now,
        onBack = onBack,
        onRecenter = {
            fixes.lastOrNull()?.let { moveCamera(CameraUpdateFactory.newLatLngZoom(it.location.toLatLng(), FOLLOW_ZOOM)) }
        },
        onZoomIn = { moveCamera(CameraUpdateFactory.zoomIn()) },
        onZoomOut = { moveCamera(CameraUpdateFactory.zoomOut()) },
        onShare = onShare,
        onOpenInMaps = onOpenInMaps,
        modifier = modifier
    ) {
        TelemetryMap(cameraPositionState = camera) {
            if (fixes.size > 1) {
                Polyline(
                    points = fixes.map { it.location.toLatLng() },
                    color = LuminousViolet,
                    width = TRAIL_WIDTH_PX
                )
            }
            fixes.lastOrNull()?.let {
                Circle(
                    center = it.location.toLatLng(),
                    radius = BEACON_RADIUS_METERS,
                    fillColor = ElectricIndigo.copy(alpha = 0.25f),
                    strokeColor = ElectricIndigo,
                    strokeWidth = 4f
                )
            }
        }
    }
}
