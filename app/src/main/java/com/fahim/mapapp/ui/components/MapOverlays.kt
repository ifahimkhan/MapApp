package com.fahim.mapapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.GlassBorder
import com.fahim.mapapp.ui.theme.SurfaceLevel1
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary

private val ControlShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(50)

/** Glass pill floating on the map, e.g. the coordinate readout or a target label. */
@Composable
fun MapPill(
    text: String,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .glass(PillShape, SurfaceLevel1.copy(alpha = 0.8f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        leading?.invoke()
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
    }
}

/** Vertical stack of square glass map buttons. Any null action is omitted. */
@Composable
fun MapControls(
    modifier: Modifier = Modifier,
    onRecenter: (() -> Unit)? = null,
    onZoomIn: (() -> Unit)? = null,
    onZoomOut: (() -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onRecenter != null) {
            MapControlButton(MyLocationIcon, "Recenter on device", onRecenter)
        }
        if (onZoomIn != null && onZoomOut != null) {
            Column(modifier = Modifier.glass(ControlShape)) {
                MapControlButton(Icons.Default.Add, "Zoom in", onZoomIn, framed = false)
                HorizontalDivider(modifier = Modifier.size(width = 52.dp, height = 1.dp), color = GlassBorder)
                MapControlButton(MinusIcon, "Zoom out", onZoomOut, framed = false)
            }
        }
    }
}

@Composable
private fun MapControlButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    framed: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .then(if (framed) Modifier.glass(ControlShape) else Modifier)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = TextPrimary)
    }
}

/** Gradient-tinted stand-in for GoogleMap, used in previews and before the map loads. */
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(ElectricIndigo.copy(alpha = 0.18f), SurfaceLevel1, SurfaceLevel1)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(TrackingGlow),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary)
        }
    }
}

