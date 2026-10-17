package com.fahim.mapapp.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.data.DeviceLocation
import com.fahim.mapapp.data.Fix
import com.fahim.mapapp.data.LoadState
import com.fahim.mapapp.data.formatDms
import com.fahim.mapapp.data.formatElapsed
import com.fahim.mapapp.ui.components.BrandHeader
import com.fahim.mapapp.ui.components.IconGlyph
import com.fahim.mapapp.ui.components.MapControls
import com.fahim.mapapp.ui.components.MapPill
import com.fahim.mapapp.ui.components.MyLocationIcon
import com.fahim.mapapp.ui.components.OpenInNewIcon
import com.fahim.mapapp.ui.components.SectionLabel
import com.fahim.mapapp.ui.components.Signal
import com.fahim.mapapp.ui.components.StatusChip
import com.fahim.mapapp.ui.components.StatusDot
import com.fahim.mapapp.ui.components.TelemetryButton
import com.fahim.mapapp.ui.components.TelemetryButtonStyle
import com.fahim.mapapp.ui.components.pod
import com.fahim.mapapp.ui.theme.EmeraldTint
import com.fahim.mapapp.ui.theme.Error
import com.fahim.mapapp.ui.theme.TextPrimary
import java.util.Locale

private val MapHeight = 360.dp

/** Everything the live session screen renders. */
data class LiveSessionUiState(
    val deviceId: String,
    val location: LoadState<DeviceLocation?> = LoadState.Loading,
    val fixes: List<Fix> = emptyList(),
    val sessionStartMillis: Long = 0L
) {
    val latest: Fix? get() = fixes.lastOrNull()
}

/** Follows one device: map with its trail, live readouts, fix timeline and share actions. */
@Composable
fun LiveSessionScreen(
    state: LiveSessionUiState,
    nowMillis: Long,
    onBack: () -> Unit,
    onRecenter: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onShare: (DeviceLocation) -> Unit,
    onOpenInMaps: (DeviceLocation) -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        BrandHeader(
            section = "Live Session",
            statusText = sessionStatus(state),
            statusSignal = if (state.latest != null) Signal.Online else Signal.Idle,
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            SessionMap(state, nowMillis, onRecenter, onZoomIn, onZoomOut, mapContent)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ReadoutStrip(state, nowMillis)
                StatusCard(state)
                FixTimeline(state.fixes, nowMillis)
                state.latest?.location?.let { location ->
                    ShareActions(location, onShare, onOpenInMaps)
                }
            }
        }
    }
}

private fun sessionStatus(state: LiveSessionUiState): String = when (val loc = state.location) {
    LoadState.Loading -> "${state.deviceId} • Connecting…"
    is LoadState.Failed -> "${state.deviceId} • Stream error"
    is LoadState.Ready -> if (loc.data == null) "${state.deviceId} • No fix yet" else "${state.deviceId} • Streaming"
}

@Composable
private fun SessionMap(
    state: LiveSessionUiState,
    nowMillis: Long,
    onRecenter: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    mapContent: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(MapHeight)) {
        mapContent()
        MapPill(
            text = "SESSION ${formatDuration(nowMillis - state.sessionStartMillis)}",
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            leading = { StatusDot(Signal.Online.foreground, size = 8.dp, pulse = state.latest != null) }
        )
        MapControls(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            onRecenter = onRecenter.takeIf { state.latest != null },
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut
        )
        state.latest?.let { fix ->
            MapPill(
                text = formatDms(fix.location),
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                leading = { Icon(MyLocationIcon, null, tint = TextPrimary, modifier = Modifier.size(14.dp)) }
            )
        }
    }
}

@Composable
private fun ReadoutStrip(state: LiveSessionUiState, nowMillis: Long) {
    val location = state.latest?.location
    Row(
        modifier = Modifier.fillMaxWidth().pod().padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Readout("Latitude", location?.let { String.format(Locale.US, "%.5f", it.latitude) } ?: "—")
        Readout("Longitude", location?.let { String.format(Locale.US, "%.5f", it.longitude) } ?: "—")
        Readout("Last fix", state.latest?.let { formatElapsed(nowMillis - it.receivedAtMillis) } ?: "—")
    }
}

@Composable
private fun Readout(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionLabel(label)
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    }
}

@Composable
private fun StatusCard(state: LiveSessionUiState) {
    val failed = state.location as? LoadState.Failed
    Row(
        modifier = Modifier.fillMaxWidth().pod().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconGlyph(shape = MaterialTheme.shapes.small, background = EmeraldTint) {
            Icon(
                if (failed != null) Icons.Default.Warning else MyLocationIcon,
                contentDescription = null,
                tint = if (failed != null) Error else Signal.Online.foreground
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            SectionLabel(
                if (failed != null) "Stream error" else "Realtime link",
                color = if (failed != null) Error else Signal.Online.foreground
            )
            Text(
                text = failed?.message ?: "Following ${state.deviceId} via Firebase",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        StatusChip("${state.fixes.size} fixes", Signal.Tracking)
    }
}

@Composable
private fun ShareActions(
    location: DeviceLocation,
    onShare: (DeviceLocation) -> Unit,
    onOpenInMaps: (DeviceLocation) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TelemetryButton(
            text = "Share",
            onClick = { onShare(location) },
            style = TelemetryButtonStyle.Secondary,
            leadingIcon = Icons.Default.Share,
            modifier = Modifier.weight(1f)
        )
        TelemetryButton(
            text = "Google Maps",
            onClick = { onOpenInMaps(location) },
            style = TelemetryButtonStyle.Secondary,
            leadingIcon = OpenInNewIcon,
            modifier = Modifier.weight(1f)
        )
    }
}

/** `00:42:17` style elapsed time. */
fun formatDuration(elapsedMillis: Long): String {
    val total = (elapsedMillis / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60)
}
