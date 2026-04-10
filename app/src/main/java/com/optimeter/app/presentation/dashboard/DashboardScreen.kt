package com.optimeter.app.presentation.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.dashboard.components.BottomNavBar
import com.optimeter.app.presentation.dashboard.components.DashboardTab
import com.optimeter.app.presentation.dashboard.tabs.HistoryTab
import com.optimeter.app.presentation.dashboard.tabs.HomeTab
import com.optimeter.app.presentation.dashboard.tabs.SettingsTab
import com.optimeter.app.presentation.dashboard.tabs.StatisticsTab
import com.optimeter.app.presentation.dashboard.tabs.AddReadingTab
import com.optimeter.app.presentation.scan.UtilitySelectionDialog
import com.optimeter.app.R

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToScanner: (MeterType, String?) -> Unit,
    onNavigateToManual: (MeterType, String?) -> Unit,
    onNavigateToHistoryDetail: (String) -> Unit,
    onNavigateToIoTDevices: () -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(DashboardTab.HOME) }

    var showUtilityDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<MeterType?>(null) }
    var pendingHomeId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Keep the + icon but do not open the dialog from here.
                    IconButton(onClick = { /* no-op; primary Add button opens the dialog */ }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_new_reading)
                        )
                    }
                }
            )
        },
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
                    },
                    onAddNewReading = { homeId ->
                        pendingHomeId = homeId
                        showUtilityDialog = true
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

            if (showUtilityDialog) {
                UtilitySelectionDialog(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it },
                    onCancel = {
                        showUtilityDialog = false
                        selectedType = null
                        pendingHomeId = null
                    },
                    onConfirm = {
                        val type = selectedType ?: MeterType.GAS
                        // Navigate to the Scanner (camera) screen with the selected meter type and homeId
                        onNavigateToScanner(type, pendingHomeId)
                        showUtilityDialog = false
                        selectedType = null
                        pendingHomeId = null
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderTab(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Tab Content", style = MaterialTheme.typography.bodyLarge)
    }
}