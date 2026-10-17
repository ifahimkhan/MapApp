package com.fahim.mapapp.ui.common

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fahim.mapapp.ui.map.hasLocationPermission
import kotlinx.coroutines.delay

private const val TICK_MILLIS = 1_000L

/** Wall-clock time that refreshes every second, for "updated 5s ago" style labels. */
@Composable
fun rememberNowMillis(): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) {
        delay(TICK_MILLIS)
        value = System.currentTimeMillis()
    }
}

private val REQUIRED_PERMISSIONS: Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

/**
 * Returns a runner that executes an action right away when location permission is held,
 * or asks for it first and runs the action only if the user grants it.
 */
@Composable
fun rememberLocationPermissionGate(): (action: () -> Unit) -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            pending?.invoke()
        } else {
            Toast.makeText(context, "Location permission is needed for tracking", Toast.LENGTH_SHORT).show()
        }
        pending = null
    }
    return remember(launcher) {
        { action ->
            if (context.hasLocationPermission()) {
                action()
            } else {
                pending = action
                launcher.launch(REQUIRED_PERMISSIONS)
            }
        }
    }
}
