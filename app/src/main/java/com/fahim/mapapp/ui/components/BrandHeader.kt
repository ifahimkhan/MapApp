package com.fahim.mapapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.theme.Canvas as CanvasColor
import com.fahim.mapapp.ui.theme.DeepViolet
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.Primary
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

private const val SPOKES = 8

/** App mark: a gradient tile with a beacon ring and radiating ticks. */
@Composable
fun RadarLogo(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Brush.linearGradient(listOf(ElectricIndigo, DeepViolet))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val radius = this.size.minDimension / 2
            val stroke = radius * 0.12f
            drawCircle(Color.White, radius * 0.22f)
            drawCircle(Color.White, radius * 0.48f, style = Stroke(stroke))
            repeat(SPOKES) { i ->
                val angle = Math.toRadians(i * 360.0 / SPOKES)
                val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                drawLine(
                    color = Color.White,
                    start = center + dir * (radius * 0.72f),
                    end = center + dir * (radius * 0.95f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Sticky top bar: "MapApp • {section}" with a live status line underneath.
 * Shows a back arrow instead of the logo's leading slot when [onBack] is set.
 */
@Composable
fun BrandHeader(
    section: String,
    statusText: String,
    statusSignal: Signal,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CanvasColor.copy(alpha = 0.94f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
        RadarLogo()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextPrimary)) { append("MapApp") }
                    withStyle(SpanStyle(color = TextSecondary)) { append(" • ") }
                    withStyle(SpanStyle(color = Primary)) { append(section) }
                },
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusDot(color = statusSignal.foreground, size = 8.dp, pulse = statusSignal == Signal.Online)
                Text(
                    text = statusText.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusSignal.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
