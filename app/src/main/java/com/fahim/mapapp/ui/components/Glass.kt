package com.fahim.mapapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.theme.GlassBorder
import com.fahim.mapapp.ui.theme.GlassBorderFaint
import com.fahim.mapapp.ui.theme.GlyphBackground
import com.fahim.mapapp.ui.theme.SurfaceLevel1
import com.fahim.mapapp.ui.theme.SurfaceLevel2

/** Fine top-lit stroke that fades out toward the bottom edge. */
val GlassStroke = Brush.verticalGradient(listOf(GlassBorder, GlassBorderFaint))

/** Surface level 1 (cards) is translucent so the map tints through. */
val GlassLevel1 = SurfaceLevel1.copy(alpha = 0.85f)

/** Surface level 2 (floating sheets and dialogs). */
val GlassLevel2 = SurfaceLevel2.copy(alpha = 0.92f)

/** Translucent pod with a gradient hairline border. */
fun Modifier.glass(shape: Shape, color: Color = GlassLevel1): Modifier = this
    .clip(shape)
    .background(color)
    .border(1.dp, GlassStroke, shape)

/** Circular or rounded inset holding an icon, as used on telemetry tiles and device rows. */
@Composable
fun IconGlyph(
    shape: Shape,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    background: Color = GlyphBackground,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center,
        content = content
    )
}
