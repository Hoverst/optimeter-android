package com.optimeter.app.presentation.dashboard.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optimeter.app.R

enum class DashboardTab(val labelResId: Int, val icon: ImageVector) {
    HOME(R.string.home, Icons.Outlined.Home),
    ANALYTICS(R.string.analytics, Icons.Outlined.BarChart),
    ADD(R.string.add, Icons.Outlined.Memory),
    SETTINGS(R.string.settings, Icons.Outlined.Settings)
}

@Composable
fun BottomNavBar(
    currentTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    val context = LocalContext.current
    
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
                val isSelected = currentTab == tab
                val isAddTab = tab == DashboardTab.ADD
                val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { 
                        if (isAddTab) {
                            Toast.makeText(context, "This feature will be added in future updates", Toast.LENGTH_SHORT).show()
                        } else {
                            onTabSelected(tab) 
                        }
                    },
                    icon = {
                        val iconModifier = if (isSelected) {
                            Modifier
                                .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(50))
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                        } else {
                            Modifier
                        }
                        Box(modifier = iconModifier, contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = tab.icon, 
                                contentDescription = tabLabel,
                                tint = if (isAddTab) disabledColor else androidx.compose.material3.LocalContentColor.current
                            )
                        }
                    },
                    label = { 
                        Text(
                            text = tabLabel,
                            color = if (isAddTab) disabledColor else androidx.compose.ui.graphics.Color.Unspecified
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = if (isAddTab) disabledColor else Color(0xFF9E9E9E),
                        unselectedTextColor = if (isAddTab) disabledColor else Color(0xFF9E9E9E)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
