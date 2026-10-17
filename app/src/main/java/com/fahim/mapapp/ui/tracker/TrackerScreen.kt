package com.fahim.mapapp.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.data.DeviceLocation
import com.fahim.mapapp.data.formatDecimal
import com.fahim.mapapp.data.formatDms
import com.fahim.mapapp.data.formatElapsed
import com.fahim.mapapp.ui.components.BrandHeader
import com.fahim.mapapp.ui.components.GlassLevel2
import com.fahim.mapapp.ui.components.IconGlyph
import com.fahim.mapapp.ui.components.MapIcon
import com.fahim.mapapp.ui.components.MapPill
import com.fahim.mapapp.ui.components.MyLocationIcon
import com.fahim.mapapp.ui.components.Signal
import com.fahim.mapapp.ui.components.SmartphoneIcon
import com.fahim.mapapp.ui.components.StatusChip
import com.fahim.mapapp.ui.components.StatusDot
import com.fahim.mapapp.ui.components.StopCircleIcon
import com.fahim.mapapp.ui.components.TelemetryButton
import com.fahim.mapapp.ui.components.TelemetryButtonStyle
import com.fahim.mapapp.ui.components.TelemetryTile
import com.fahim.mapapp.ui.components.glass
import com.fahim.mapapp.ui.components.pod
import com.fahim.mapapp.ui.theme.IndigoSelected
import com.fahim.mapapp.ui.theme.Primary
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary

private const val NO_VALUE = "—"

/** What the Tracker tab knows about this phone's own broadcast. */
data class TrackerUiState(
    val trackingDeviceId: String? = null,
    val location: DeviceLocation? = null,
    val lastUpdateMillis: Long? = null
) {
    val isTracking: Boolean get() = trackingDeviceId != null
}

/**
 * Tracker tab: full-bleed map with telemetry overlays and a control panel.
 * [mapContent] is the GoogleMap in the app and a placeholder in previews.
 */
@Composable
fun TrackerScreen(
    state: TrackerUiState,
    nowMillis: Long,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onOpenSession: () -> Unit,
    onBrowseDevices: () -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        mapContent()
        Column(modifier = Modifier.fillMaxSize()) {
            BrandHeader(
                section = "Tracker",
                statusText = state.trackingDeviceId?.let { "$it • Transmitting" } ?: "Idle • Not transmitting",
                statusSignal = if (state.isTracking) Signal.Online else Signal.Idle
            )
            MapStatusRow(state)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                state.location?.let { location ->
                    MapPill(
                        text = formatDms(location),
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                        leading = {
                            Icon(MyLocationIcon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }
            TrackerPanel(
                state = state,
                nowMillis = nowMillis,
                onStartTracking = onStartTracking,
                onStopTracking = onStopTracking,
                onOpenSession = onOpenSession,
                onBrowseDevices = onBrowseDevices,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun MapStatusRow(state: TrackerUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val signal = if (state.location != null) Signal.Online else Signal.Idle
        MapPill(
            text = if (state.location != null) "GPS: LOCKED" else "GPS: STANDBY",
            leading = { StatusDot(color = signal.foreground, size = 8.dp, pulse = signal == Signal.Online) }
        )
        state.trackingDeviceId?.let { id ->
            MapPill(
                text = id,
                leading = { Icon(SmartphoneIcon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
private fun TrackerPanel(
    state: TrackerUiState,
    nowMillis: Long,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onOpenSession: () -> Unit,
    onBrowseDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(MaterialTheme.shapes.large, GlassLevel2)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PanelHeader(state, nowMillis)
        Row(
            modifier = Modifier.fillMaxWidth().pod().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text(
                text = if (state.isTracking) {
                    "This device is publishing its location to the fleet in real time."
                } else {
                    "Broadcast this device's location, or watch any device on the map."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        TelemetryGrid(state)
        PanelActions(state, onStartTracking, onStopTracking, onOpenSession, onBrowseDevices)
    }
}

@Composable
private fun PanelHeader(state: TrackerUiState, nowMillis: Long) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        IconGlyph(shape = MaterialTheme.shapes.medium, size = 52.dp, background = IndigoSelected) {
            Icon(MyLocationIcon, contentDescription = null, tint = Primary, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Location Tracker", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                if (state.isTracking) StatusChip("Live", Signal.Online) else StatusChip("Idle", Signal.Idle)
            }
            Text(
                text = state.lastUpdateMillis
                    ?.let { "Updated ${formatElapsed(nowMillis - it).lowercase()}" }
                    ?: if (state.isTracking) "Waiting for first fix…" else "Tracking is off",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        if (state.isTracking && state.lastUpdateMillis == null) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun TelemetryGrid(state: TrackerUiState) {
    val location = state.location
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryTile("Device ID", state.trackingDeviceId ?: NO_VALUE, SmartphoneIcon, Modifier.weight(1f))
            TelemetryTile(
                label = "Status",
                value = if (state.isTracking) "Transmitting" else "Idle",
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f),
                iconTint = if (state.isTracking) Signal.Online.foreground else TextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryTile("Latitude", location?.let { formatDecimal(it.latitude) } ?: NO_VALUE, Icons.Default.LocationOn, Modifier.weight(1f))
            TelemetryTile("Longitude", location?.let { formatDecimal(it.longitude) } ?: NO_VALUE, Icons.Default.LocationOn, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PanelActions(
    state: TrackerUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onOpenSession: () -> Unit,
    onBrowseDevices: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.isTracking) {
            TelemetryButton("Open Live Session", onOpenSession, leadingIcon = MapIcon)
            TelemetryButton("Stop Tracking", onStopTracking, style = TelemetryButtonStyle.Destructive, leadingIcon = StopCircleIcon)
        } else {
            TelemetryButton("Start Active Tracking", onStartTracking, leadingIcon = Icons.Default.PlayArrow)
            TelemetryButton("Track a Device on the Map", onBrowseDevices, style = TelemetryButtonStyle.Secondary, leadingIcon = MapIcon)
        }
    }
}
