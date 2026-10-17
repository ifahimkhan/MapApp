package com.fahim.mapapp.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fahim.mapapp.ui.components.GlassLevel2
import com.fahim.mapapp.ui.components.TelemetryButton
import com.fahim.mapapp.ui.components.glass
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.GlassBorder
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary

/** Firebase keys can't contain `. # $ [ ] /`, so an ID with them would break the write path. */
private val FORBIDDEN_KEY_CHARS = Regex("[.#$\\[\\]/]")
const val MAX_DEVICE_ID_LENGTH = 64

/** Returns an error message for an invalid device ID, or null when it is usable. */
fun validateDeviceId(raw: String): String? {
    val id = raw.trim()
    return when {
        id.isEmpty() -> "Device ID cannot be empty"
        id.length > MAX_DEVICE_ID_LENGTH -> "Keep it under $MAX_DEVICE_ID_LENGTH characters"
        FORBIDDEN_KEY_CHARS.containsMatchIn(id) -> "Can't contain . # \$ [ ] or /"
        else -> null
    }
}

/** Glass dialog asking which ID this phone should broadcast under. */
@Composable
fun DeviceIdDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() {
        val problem = validateDeviceId(text)
        if (problem == null) onConfirm(text.trim()) else error = problem
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glass(MaterialTheme.shapes.large, GlassLevel2)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Broadcast as…", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text(
                "Pick the ID other devices will see this phone as.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    error = null
                },
                label = { Text("Device ID") },
                placeholder = { Text("e.g. pixel_8") },
                isError = error != null,
                supportingText = error?.let { message -> { Text(message) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricIndigo,
                    unfocusedBorderColor = GlassBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
            TelemetryButton("Start Tracking", ::submit, leadingIcon = Icons.Default.PlayArrow)
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Cancel", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
