package com.optimeter.app.presentation.iot

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.optimeter.app.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddDeviceScreen(
    onNavigateBack: () -> Unit
) {
    var deviceId by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }

    var step by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_device)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (step == 1) {
                Text(stringResource(R.string.step1_scan_qr), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

                LaunchedEffect(Unit) {
                    if (!cameraPermissionState.status.isGranted) {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (cameraPermissionState.status.isGranted) {
                            Text("QR Scanner Ready")
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.camera_permission_required))
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                    Text(stringResource(R.string.grant_permission))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("OR")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text(stringResource(R.string.device_id)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { step = 2 },
                    enabled = deviceId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.next_wifi_setup))
                }
            } else {
                Text(stringResource(R.string.step2_wifi), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Device: $deviceId", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = wifiSsid,
                    onValueChange = { wifiSsid = it },
                    label = { Text(stringResource(R.string.wifi_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = wifiPassword,
                    onValueChange = { wifiPassword = it },
                    label = { Text(stringResource(R.string.wifi_password)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onNavigateBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.provision_ble))
                }
            }
        }
    }
}
