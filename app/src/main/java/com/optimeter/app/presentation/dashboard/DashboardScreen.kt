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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.saveable.rememberSaveable

enum class DialogIntent { CAMERA, GALLERY }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToScanner: (MeterType, String?) -> Unit,
    onNavigateToManual: (MeterType, String?) -> Unit,
    onNavigateToHistoryDetail: (String) -> Unit,
    onNavigateToIoTDevices: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToValidation: (MeterType, String, String?) -> Unit = { _, _, _ -> },
    onNavigateToHistory: (MeterType) -> Unit = {}
) {
    var currentTab by rememberSaveable { mutableStateOf(DashboardTab.HOME) }

    var showUtilityDialog by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf<MeterType?>(null) }
    var pendingHomeId by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogIntent by rememberSaveable { mutableStateOf<DialogIntent?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && selectedType != null) {
                onNavigateToValidation(selectedType!!, uri.toString(), pendingHomeId)
                selectedType = null
                pendingHomeId = null
                dialogIntent = null
            }
        }
    )

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
                    },
                    onAddNewReadingCamera = { homeId ->
                        pendingHomeId = homeId
                        dialogIntent = DialogIntent.CAMERA
                        showUtilityDialog = true
                    },
                    onAddNewReadingGallery = { homeId ->
                        pendingHomeId = homeId
                        dialogIntent = DialogIntent.GALLERY
                        showUtilityDialog = true
                    }
                )
                DashboardTab.ANALYTICS -> StatisticsTab(
                    onNavigateToHistory = onNavigateToHistory
                )
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
                        showUtilityDialog = false
                        
                        if (dialogIntent == DialogIntent.GALLERY) {
                            // Launch the photo picker, navigation will happen in onResult
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            // Navigate to the Scanner (camera) screen
                            onNavigateToScanner(type, pendingHomeId)
                            selectedType = null
                            pendingHomeId = null
                            dialogIntent = null
                        }
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