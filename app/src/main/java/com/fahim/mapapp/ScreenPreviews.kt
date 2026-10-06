package com.fahim.mapapp

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fahim.mapapp.ui.theme.MapAppTheme

/** Renders each preview in light and dark theme on a phone-sized canvas. */
@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Preview(
    name = "Dark",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class ThemePreviews

private const val PREVIEW_DEVICE_ID = "device_123"
private const val PREVIEW_LATITUDE = 25.2048
private const val PREVIEW_LONGITUDE = 55.2708
private val PREVIEW_DEVICES = listOf("device_123", "pixel_8", "office_tablet")

/** Static colors so previews look the same regardless of the host's wallpaper. */
@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    MapAppTheme(dynamicColor = false, content = content)
}

// region MainScreen

@ThemePreviews
@Composable
private fun MainScreenIdlePreview() {
    PreviewTheme {
        MainScreen(isStartButtonEnabled = true, onStartTracking = {}, onDeviceSelected = {})
    }
}

@ThemePreviews
@Composable
private fun MainScreenTrackingActivePreview() {
    PreviewTheme {
        MainScreen(isStartButtonEnabled = false, onStartTracking = {}, onDeviceSelected = {})
    }
}

// endregion

// region Dialogs

@ThemePreviews
@Composable
private fun DeviceIdDialogPreview() {
    PreviewTheme {
        DeviceIdDialog(onDismiss = {}, onConfirm = {})
    }
}

@ThemePreviews
@Composable
private fun DeviceListDialogLoadingPreview() {
    PreviewTheme {
        DeviceListDialogContent(
            devices = emptyList(),
            isLoading = true,
            errorMessage = null,
            onDismiss = {},
            onDeviceSelected = {}
        )
    }
}

@ThemePreviews
@Composable
private fun DeviceListDialogDevicesPreview() {
    PreviewTheme {
        DeviceListDialogContent(
            devices = PREVIEW_DEVICES,
            isLoading = false,
            errorMessage = null,
            onDismiss = {},
            onDeviceSelected = {}
        )
    }
}

@ThemePreviews
@Composable
private fun DeviceListDialogEmptyPreview() {
    PreviewTheme {
        DeviceListDialogContent(
            devices = emptyList(),
            isLoading = false,
            errorMessage = null,
            onDismiss = {},
            onDeviceSelected = {}
        )
    }
}

@ThemePreviews
@Composable
private fun DeviceListDialogErrorPreview() {
    PreviewTheme {
        DeviceListDialogContent(
            devices = emptyList(),
            isLoading = false,
            errorMessage = "Permission denied",
            onDismiss = {},
            onDeviceSelected = {}
        )
    }
}

// endregion

// region MapsScreen

/** Stand-in for GoogleMap, which cannot render in Android Studio previews. */
@Composable
private fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@ThemePreviews
@Composable
private fun MapsScreenDevicePreview() {
    PreviewTheme {
        MapsScreenContent(
            selectedDeviceId = PREVIEW_DEVICE_ID,
            latitude = PREVIEW_LATITUDE,
            longitude = PREVIEW_LONGITUDE,
            onBackClick = {},
            mapContent = { MapPlaceholder() }
        )
    }
}

@ThemePreviews
@Composable
private fun MapsScreenNoDevicePreview() {
    PreviewTheme {
        MapsScreenContent(
            selectedDeviceId = "",
            latitude = PREVIEW_LATITUDE,
            longitude = PREVIEW_LONGITUDE,
            onBackClick = {},
            mapContent = { MapPlaceholder() }
        )
    }
}

// endregion
