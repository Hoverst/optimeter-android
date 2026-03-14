package com.optimeter.app.presentation.iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optimeter.app.R
import com.optimeter.app.domain.model.Device
import com.optimeter.app.domain.model.MeterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IoTDevicesScreen(
    onNavigateBack: () -> Unit,
    onAddDeviceClick: () -> Unit
) {
    // Stub devices
    val devices = listOf(
        Device("ESP_001", "Home Water Meter", MeterType.WATER, true, 85),
        Device("ESP_002", "Parent's Gas Meter", MeterType.GAS, false, 15)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_devices)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDeviceClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_device))
            }
        }
    ) { padding ->
        if (devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_readings))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(devices) { device ->
                    DeviceCard(device)
                }
            }
        }
    }
}

@Composable
fun DeviceCard(device: Device) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (device.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (device.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (device.isOnline) stringResource(R.string.online)
                               else stringResource(R.string.offline),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (device.batteryPercentage < 20) Icons.Default.Warning else Icons.Default.BatteryFull,
                    contentDescription = stringResource(R.string.battery),
                    tint = if (device.batteryPercentage < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text("${device.batteryPercentage}%", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
