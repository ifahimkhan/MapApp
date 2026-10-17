package com.fahim.mapapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.theme.DangerBorder
import com.fahim.mapapp.ui.theme.DangerLight
import com.fahim.mapapp.ui.theme.DangerTint
import com.fahim.mapapp.ui.theme.DeepViolet
import com.fahim.mapapp.ui.theme.ElectricIndigo
import com.fahim.mapapp.ui.theme.IndigoBorder
import com.fahim.mapapp.ui.theme.IndigoLight
import com.fahim.mapapp.ui.theme.IndigoTint
import com.fahim.mapapp.ui.theme.TextPrimary

/** Visual variants from the design system's button spec. */
enum class TelemetryButtonStyle { Primary, Secondary, Destructive }

private const val PRESSED_SCALE = 0.98f
private const val DISABLED_ALPHA = 0.45f
private val MinButtonHeight = 52.dp

private val PrimaryGradient = Brush.horizontalGradient(listOf(ElectricIndigo, DeepViolet))

/**
 * Full-width action button. Primary uses the indigo-to-violet gradient with a glow,
 * Secondary is the indigo-tinted outline, Destructive the red outline.
 */
@Composable
fun TelemetryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TelemetryButtonStyle = TelemetryButtonStyle.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val base = modifier
        .fillMaxWidth()
        .heightIn(min = MinButtonHeight)
        .scale(if (pressed && enabled) PRESSED_SCALE else 1f)
        .alpha(if (enabled) 1f else DISABLED_ALPHA)

    val styled = when (style) {
        TelemetryButtonStyle.Primary -> base
            .shadow(if (enabled) 16.dp else 0.dp, shape, ambientColor = ElectricIndigo, spotColor = ElectricIndigo)
            .clip(shape)
            .background(PrimaryGradient)
        TelemetryButtonStyle.Secondary -> base
            .clip(shape)
            .background(IndigoTint)
            .border(1.dp, IndigoBorder, shape)
        TelemetryButtonStyle.Destructive -> base
            .clip(shape)
            .background(DangerTint)
            .border(1.dp, DangerBorder, shape)
    }

    val contentColor = when (style) {
        TelemetryButtonStyle.Primary -> TextPrimary
        TelemetryButtonStyle.Secondary -> IndigoLight
        TelemetryButtonStyle.Destructive -> DangerLight
    }

    Row(
        modifier = styled
            .clickable(
                interactionSource = interaction,
                indication = rememberRipple(color = contentColor),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ButtonContent(text, leadingIcon, contentColor)
    }
}

@Composable
private fun RowScope.ButtonContent(text: String, icon: ImageVector?, color: Color) {
    CompositionLocalProvider(LocalContentColor provides color) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleSmall, color = color)
    }
}
