package com.fahim.mapapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fahim.mapapp.ui.components.DevicesIcon
import com.fahim.mapapp.ui.components.MyLocationIcon
import com.fahim.mapapp.ui.components.glass
import com.fahim.mapapp.ui.theme.Canvas
import com.fahim.mapapp.ui.theme.Primary
import com.fahim.mapapp.ui.theme.TextSecondary

/** Bottom navigation destinations. */
enum class HomeTab(val label: String, val icon: ImageVector) {
    Tracker("Tracker", MyLocationIcon),
    Devices("Devices", DevicesIcon)
}

/** Shell with the floating glass bottom bar; [content] renders the selected tab. */
@Composable
fun HomeScreen(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    content: @Composable (HomeTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content(selectedTab)
        }
        BottomBar(selectedTab, onTabSelected)
    }
}

@Composable
private fun BottomBar(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Canvas)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .glass(MaterialTheme.shapes.large)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val color = if (isSelected) Primary else TextSecondary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) })
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
                Text(tab.label, style = MaterialTheme.typography.labelLarge, color = color)
            }
        }
    }
}
