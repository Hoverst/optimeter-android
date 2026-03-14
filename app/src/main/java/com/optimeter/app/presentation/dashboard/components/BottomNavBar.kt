package com.optimeter.app.presentation.dashboard.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optimeter.app.R
import com.optimeter.app.ui.theme.Chart1

enum class DashboardTab(val labelResId: Int, val icon: ImageVector) {
    HOME(R.string.home, Icons.Outlined.Home),
    ANALYTICS(R.string.analytics, Icons.Outlined.BarChart),
    ADD(R.string.add, Icons.Outlined.CameraAlt),
    SETTINGS(R.string.settings, Icons.Outlined.Settings)
}

@Composable
fun BottomNavBar(
    currentTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    androidx.compose.foundation.layout.Column {
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp, // Flat design like React
            windowInsets = WindowInsets(0, 0, 0, 0), // Remove default navbar insets that might push it up
            modifier = Modifier.height(72.dp)
        ) {
            DashboardTab.values().forEach { tab ->
                val tabLabel = stringResource(id = tab.labelResId)
                NavigationBarItem(
                    selected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = { Icon(tab.icon, contentDescription = tabLabel) },
                    label = { Text(tabLabel) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Chart1,
                        selectedTextColor = Chart1,
                        indicatorColor = Color.Transparent, // Figma design doesn't have the pill highlight
                        unselectedIconColor = Color(0xFF9E9E9E), // Light gray for visibility
                        unselectedTextColor = Color(0xFF9E9E9E)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
