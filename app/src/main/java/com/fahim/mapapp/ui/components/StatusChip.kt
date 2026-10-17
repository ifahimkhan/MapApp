package com.fahim.mapapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.EmeraldLight
import com.fahim.mapapp.ui.theme.EmeraldTint
import com.fahim.mapapp.ui.theme.IndigoLight
import com.fahim.mapapp.ui.theme.IndigoSelected
import com.fahim.mapapp.ui.theme.SurfaceContainerHighest
import com.fahim.mapapp.ui.theme.TextSecondary
import com.fahim.mapapp.ui.theme.TextTertiary

/** Semantic states shared by chips and beacons. */
enum class Signal(val foreground: Color, val background: Color) {
    Online(EmeraldLight, EmeraldTint),
    Tracking(IndigoLight, IndigoSelected),
    Idle(TextSecondary, SurfaceContainerHighest),
    Offline(TextTertiary, SurfaceContainerHighest)
}

/** Status beacon dot; [pulse] makes it breathe to signal a live stream. */
@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 6.dp,
    pulse: Boolean = false
) {
    val alpha = if (pulse) {
        val transition = rememberInfiniteTransition(label = "beacon")
        val value by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "beaconAlpha"
        )
        value
    } else {
        1f
    }
    Box(
        modifier = modifier
            .size(size)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}

/** Compact uppercase badge such as LIVE, ONLINE or IDLE. */
@Composable
fun StatusChip(
    text: String,
    signal: Signal,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(signal.background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showDot) StatusDot(color = signal.foreground, pulse = signal == Signal.Online)
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = signal.foreground
        )
    }
}

/** The indigo glow used for the "tracking" beacon, exposed for map overlays. */
val TrackingGlow: Color = ElectricIndigo.copy(alpha = 0.35f)
