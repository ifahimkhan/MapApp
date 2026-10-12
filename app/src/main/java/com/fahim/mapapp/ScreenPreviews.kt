package com.fahim.mapapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fahim.mapapp.data.Device
import com.fahim.mapapp.data.DeviceLocation
import com.fahim.mapapp.data.Fix
import com.fahim.mapapp.data.LoadState
import com.fahim.mapapp.ui.components.MapPlaceholder
import com.fahim.mapapp.ui.devices.DevicesScreen
import com.fahim.mapapp.ui.home.HomeScreen
import com.fahim.mapapp.ui.home.HomeTab
import com.fahim.mapapp.ui.session.LiveSessionScreen
import com.fahim.mapapp.ui.session.LiveSessionUiState
import com.fahim.mapapp.ui.theme.MapAppTheme
import com.fahim.mapapp.ui.tracker.DeviceIdDialog
import com.fahim.mapapp.ui.tracker.TrackerScreen
import com.fahim.mapapp.ui.tracker.TrackerUiState

/** Phone-sized preview with system bars; the app is dark-only. */
@Preview(name = "Phone", showBackground = true, showSystemUi = true, backgroundColor = 0xFF0B0F17)
annotation class PhonePreview


private const val NOW = 1_760_000_000_000L
private val DUBAI = DeviceLocation(25.2048, 55.2708)
private val PREVIEW_DEVICES = listOf(
    Device("device_123", DeviceLocation(25.2101, 55.2799)),
    Device("office_tablet", DeviceLocation(25.1972, 55.2744)),
    Device("pixel_8", DUBAI),
    Device("ranger_band_04", null)
)
private val PREVIEW_FIXES = (0 until 5).map { i ->
    Fix(DeviceLocation(25.2048 + i * 0.0012, 55.2708 + i * 0.0009), NOW - (5 - i) * 10_000L)
}

@Composable
private fun TrackerPreview(state: TrackerUiState) {
    MapAppTheme {
        HomeScreen(selectedTab = HomeTab.Tracker, onTabSelected = {}) {
            TrackerScreen(
                state = state,
                nowMillis = NOW,
                onStartTracking = {},
                onStopTracking = {},
                onOpenSession = {},
                onBrowseDevices = {},
                mapContent = { MapPlaceholder() }
            )
        }
    }
}

@PhonePreview
@Composable
private fun TrackerIdlePreview() = TrackerPreview(TrackerUiState())

@PhonePreview
@Composable
private fun TrackerLivePreview() = TrackerPreview(
    TrackerUiState(trackingDeviceId = "pixel_8", location = DUBAI, lastUpdateMillis = NOW - 2_000)
)

@Composable
private fun DevicesPreview(devices: LoadState<List<Device>>, selectedId: String? = null) {
    MapAppTheme {
        HomeScreen(selectedTab = HomeTab.Devices, onTabSelected = {}) {
            DevicesScreen(
                devices = devices,
                query = "",
                selectedId = selectedId,
                onQueryChange = {},
                onSelect = {},
                onConfirm = {},
                mapContent = { MapPlaceholder() }
            )
        }
    }
}

@PhonePreview
@Composable
private fun DevicesListPreview() = DevicesPreview(LoadState.Ready(PREVIEW_DEVICES), selectedId = "pixel_8")

@PhonePreview
@Composable
private fun DevicesLoadingPreview() = DevicesPreview(LoadState.Loading)

@PhonePreview
@Composable
private fun DevicesEmptyPreview() = DevicesPreview(LoadState.Ready(emptyList()))

@PhonePreview
@Composable
private fun DevicesErrorPreview() = DevicesPreview(LoadState.Failed("Permission denied"))

@PhonePreview
@Composable
private fun LiveSessionPreview() {
    MapAppTheme {
        LiveSessionScreen(
            state = LiveSessionUiState(
                deviceId = "pixel_8",
                location = LoadState.Ready(PREVIEW_FIXES.last().location),
                fixes = PREVIEW_FIXES,
                sessionStartMillis = NOW - 2_537_000
            ),
            nowMillis = NOW,
            onBack = {},
            onRecenter = {},
            onZoomIn = {},
            onZoomOut = {},
            onShare = {},
            onOpenInMaps = {},
            mapContent = { MapPlaceholder() }
        )
    }
}

@PhonePreview
@Composable
private fun LiveSessionWaitingPreview() {
    MapAppTheme {
        LiveSessionScreen(
            state = LiveSessionUiState(deviceId = "pixel_8", location = LoadState.Ready(null), sessionStartMillis = NOW),
            nowMillis = NOW,
            onBack = {},
            onRecenter = {},
            onZoomIn = {},
            onZoomOut = {},
            onShare = {},
            onOpenInMaps = {},
            mapContent = { MapPlaceholder() }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F17)
@Composable
private fun DeviceIdDialogPreview() {
    MapAppTheme {
        DeviceIdDialog(onDismiss = {}, onConfirm = {})
    }
}
