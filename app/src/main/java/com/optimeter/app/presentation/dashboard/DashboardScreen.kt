package com.optimeter.app.presentation.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.dashboard.components.BottomNavBar
import com.optimeter.app.presentation.dashboard.components.DashboardTab
import com.optimeter.app.presentation.dashboard.tabs.HistoryTab
import com.optimeter.app.presentation.dashboard.tabs.HomeTab
import com.optimeter.app.presentation.dashboard.tabs.SettingsTab
import com.optimeter.app.presentation.dashboard.tabs.StatisticsTab
import com.optimeter.app.presentation.dashboard.tabs.AddReadingTab

@Composable
fun DashboardScreen(
    onNavigateToScanner: (MeterType, String?) -> Unit,
    onNavigateToHistoryDetail: (String) -> Unit,
    onNavigateToIoTDevices: () -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(DashboardTab.HOME) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                DashboardTab.HOME -> HomeTab(
                    userName = "User", // This should come from a ViewModel later
                    onMeterSelected = { meterType, homeId ->
                        onNavigateToScanner(meterType, homeId)
                    }
                )
                DashboardTab.ANALYTICS -> StatisticsTab()
                DashboardTab.ADD -> AddReadingTab()
                DashboardTab.SETTINGS -> SettingsTab(
                    onNavigateToIoTDevices = onNavigateToIoTDevices,
                    onLogout = onLogout,
                    onDeleteAccount = onLogout
                )
            }
        }
    }
}

@Composable
fun PlaceholderTab(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Tab Content")
    }
}