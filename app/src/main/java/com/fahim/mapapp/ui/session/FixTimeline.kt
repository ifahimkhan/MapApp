package com.fahim.mapapp.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.data.Fix
import com.fahim.mapapp.data.formatDecimal
import com.fahim.mapapp.data.formatElapsed
import com.fahim.mapapp.ui.components.SectionLabel
import com.fahim.mapapp.ui.components.Signal
import com.fahim.mapapp.ui.components.StatusChip
import com.fahim.mapapp.ui.components.StatusDot
import com.fahim.mapapp.ui.components.pod
import com.fahim.mapapp.ui.theme.IndigoLight
import com.fahim.mapapp.ui.theme.TextPrimary
import com.fahim.mapapp.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val VISIBLE_FIXES = 4

/** Most recent fixes received this session, newest first. */
@Composable
fun FixTimeline(fixes: List<Fix>, nowMillis: Long, modifier: Modifier = Modifier) {
    val clock = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Column(
        modifier = modifier.fillMaxWidth().pod().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SectionLabel("Telemetry fix timeline")
            SectionLabel("● Live", color = IndigoLight)
        }
        if (fixes.isEmpty()) {
            Text("Waiting for the first fix…", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        fixes.takeLast(VISIBLE_FIXES).asReversed().forEachIndexed { index, fix ->
            val isLatest = index == 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusDot(
                    color = if (isLatest) Signal.Online.foreground else IndigoLight,
                    size = 12.dp,
                    pulse = isLatest
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${formatDecimal(fix.location.latitude)}, ${formatDecimal(fix.location.longitude)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        "${clock.format(Date(fix.receivedAtMillis))} • ${formatElapsed(nowMillis - fix.receivedAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (isLatest) StatusChip("Latest", Signal.Online)
            }
        }
    }
}
