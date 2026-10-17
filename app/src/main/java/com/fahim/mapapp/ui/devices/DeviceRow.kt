package com.fahim.mapapp.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.data.Device
import com.fahim.mapapp.data.formatDecimal
import com.fahim.mapapp.ui.components.IconGlyph
import com.fahim.mapapp.ui.components.Signal
import com.fahim.mapapp.ui.components.SmartphoneIcon
import com.fahim.mapapp.ui.components.StatusChip
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.IndigoBorder
import com.fahim.mapapp.ui.theme.IndigoSelected
import com.fahim.mapapp.ui.theme.SurfaceContainerHigh
import com.fahim.mapapp.ui.theme.SurfaceContainerHighest
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary

/** One selectable device: glyph, ID, coordinates and a located/no-fix badge. */
@Composable
fun DeviceRow(
    device: Device,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    val signal = if (device.isLocated) Signal.Online else Signal.Offline
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) IndigoSelected else SurfaceContainerHigh)
            .then(if (selected) Modifier.border(1.dp, IndigoBorder, shape) else Modifier)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconGlyph(shape = MaterialTheme.shapes.small, size = 48.dp) {
            Icon(
                SmartphoneIcon,
                contentDescription = null,
                tint = if (device.isLocated) TextPrimary else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = device.id,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                StatusChip(if (device.isLocated) "Located" else "No fix", signal)
            }
            Text(
                text = device.location
                    ?.let { "${formatDecimal(it.latitude)}, ${formatDecimal(it.longitude)}" }
                    ?: "No coordinates reported",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        SelectionMark(selected)
    }
}

@Composable
private fun SelectionMark(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (selected) ElectricIndigo else SurfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
        }
    }
}
