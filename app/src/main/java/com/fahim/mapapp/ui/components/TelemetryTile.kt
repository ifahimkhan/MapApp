package com.fahim.mapapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.theme.SurfaceContainerHigh
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary

/** Labelled metric pod: icon glyph on the left, small caps label over a large value. */
@Composable
fun TelemetryTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(SurfaceContainerHigh)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconGlyph(shape = MaterialTheme.shapes.small, size = 40.dp) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Small caps section header like "CONNECTED NODES (4)". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = TextSecondary) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier
    )
}

/** Plain rounded pod used to frame a block of content with a slightly raised surface. */
fun Modifier.pod(): Modifier = this
    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
    .background(SurfaceContainerHigh)
