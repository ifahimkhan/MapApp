package com.fahim.mapapp.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.data.Device
import com.fahim.mapapp.data.LoadState
import com.fahim.mapapp.data.filterDevices
import com.fahim.mapapp.ui.components.BrandHeader
import com.fahim.mapapp.ui.components.DevicesIcon
import com.fahim.mapapp.ui.components.GlassLevel2
import com.fahim.mapapp.ui.components.MapPill
import com.fahim.mapapp.ui.components.SectionLabel
import com.fahim.mapapp.ui.components.Signal
import com.fahim.mapapp.ui.components.StatusDot
import com.fahim.mapapp.ui.components.TelemetryButton
import com.fahim.mapapp.ui.components.glass
import com.fahim.mapapp.ui.theme.Canvas
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.Error
import com.fahim.mapapp.ui.theme.GlassBorder
import com.fahim.mapapp.ui.theme.Primary
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary

private val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val MapPeekHeight = 220.dp

/**
 * Devices tab: a map of every located device above a searchable device sheet.
 * Selecting a row arms the confirm button, which opens the live session.
 */
@Composable
fun DevicesScreen(
    devices: LoadState<List<Device>>,
    query: String,
    selectedId: String?,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        BrandHeader(
            section = "Devices",
            statusText = devicesStatus(devices),
            statusSignal = if (devices is LoadState.Ready && devices.data.any { it.isLocated }) Signal.Online else Signal.Idle
        )
        Box(modifier = Modifier.fillMaxWidth().height(MapPeekHeight)) {
            mapContent()
            selectedId?.let { id ->
                MapPill(
                    text = "TARGET: $id",
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    leading = { StatusDot(Signal.Online.foreground, size = 8.dp) }
                )
            }
        }
        DeviceSheet(
            devices = devices,
            query = query,
            selectedId = selectedId,
            onQueryChange = onQueryChange,
            onSelect = onSelect,
            onConfirm = onConfirm,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun devicesStatus(devices: LoadState<List<Device>>): String = when (devices) {
    LoadState.Loading -> "Syncing fleet…"
    is LoadState.Failed -> "Fleet unavailable"
    is LoadState.Ready -> "${devices.data.count { it.isLocated }} of ${devices.data.size} located"
}

@Composable
private fun DeviceSheet(
    devices: LoadState<List<Device>>,
    query: String,
    selectedId: String?,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(SheetShape, GlassLevel2)
            .padding(horizontal = 16.dp)
    ) {
        SheetHeader()
        SearchField(query, onQueryChange)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (devices) {
                LoadState.Loading -> CircularProgressIndicator(color = ElectricIndigo, modifier = Modifier.align(Alignment.Center))
                is LoadState.Failed -> SheetMessage("Couldn't load devices: ${devices.message}", isError = true)
                is LoadState.Ready -> DeviceList(devices.data, query, selectedId, onSelect)
            }
        }
        TelemetryButton(
            text = "Confirm Selection & Monitor",
            onClick = { selectedId?.let(onConfirm) },
            enabled = selectedId != null,
            leadingIcon = DevicesIcon,
            modifier = Modifier.padding(vertical = 14.dp)
        )
    }
}

@Composable
private fun SheetHeader() {
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .glass(RoundedCornerShape(50), GlassBorder)
        )
        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(DevicesIcon, contentDescription = null, tint = Primary)
            Text("Select a Device", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        }
        Text(
            "Choose a broadcasting device to follow on the map.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Filter devices by ID…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear filter")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricIndigo,
            unfocusedBorderColor = GlassBorder,
            focusedContainerColor = Canvas.copy(alpha = 0.6f),
            unfocusedContainerColor = Canvas.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeviceList(
    all: List<Device>,
    query: String,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    if (all.isEmpty()) {
        SheetMessage("No devices are broadcasting yet. Start tracking on a phone to see it here.")
        return
    }
    val shown = filterDevices(all, query)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionLabel("Connected nodes (${all.size})")
            SectionLabel("● ${all.count { it.isLocated }} located", color = Signal.Online.foreground)
        }
        if (shown.isEmpty()) {
            SheetMessage("No device matches \"$query\".")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(shown, key = { it.id }) { device ->
                    DeviceRow(device, selected = device.id == selectedId, onClick = { onSelect(device.id) })
                }
            }
        }
    }
}

@Composable
private fun SheetMessage(text: String, isError: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isError) Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(28.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) Error else TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
