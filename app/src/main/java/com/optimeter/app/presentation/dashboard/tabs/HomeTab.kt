package com.optimeter.app.presentation.dashboard.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.dashboard.components.MeterCard
import com.optimeter.app.ui.theme.Chart1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    userName: String,
    onMeterSelected: (MeterType) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedHome by remember { mutableStateOf<String?>(null) }
    val homes = uiState.homes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Custom Dropdown for Home Selection
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .menuAnchor()
                    .clickable { expanded = true }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = selectedHome ?: homes.firstOrNull()?.name ?: "My Home",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Home",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                homes.forEach { home ->
                    DropdownMenuItem(
                        text = { Text(home.name) },
                        onClick = {
                            selectedHome = home.name
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onMeterSelected(MeterType.GAS) }, // Using the existing interface to trigger add
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Chart1),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Add New Reading", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Current Readings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        MeterCard(
            meterType = MeterType.ELECTRICITY, // Placed Electricity first matching screenshot
            lastReading = "11,045",
            lastReadingDate = "Mar 13",
            consumptionString = "-218.0",
            isTrendingUp = false,
            onClick = { onMeterSelected(MeterType.ELECTRICITY) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MeterCard(
            meterType = MeterType.GAS,
            lastReading = "5,347",
            lastReadingDate = "Mar 13",
            consumptionString = "-106.0",
            isTrendingUp = false,
            onClick = { onMeterSelected(MeterType.GAS) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MeterCard(
            meterType = MeterType.WATER, // Cold Water
            lastReading = "2,623",
            lastReadingDate = "Mar 13",
            consumptionString = "338.0",
            isTrendingUp = true,
            onClick = { onMeterSelected(MeterType.WATER) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // View Detailed Analytics Button
        OutlinedButton(
            onClick = { /* Navigate to Analytics manually or swap tabs */ },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text("View Detailed Analytics", fontWeight = FontWeight.Medium)
        }
    }
}
