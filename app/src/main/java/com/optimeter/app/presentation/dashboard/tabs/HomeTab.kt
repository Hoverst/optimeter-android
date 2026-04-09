package com.optimeter.app.presentation.dashboard.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.dashboard.components.MeterCard
import com.optimeter.app.ui.theme.Chart1
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    userName: String,
    onMeterSelected: (MeterType, String?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val homes = uiState.homes
    val latestReadings = uiState.latestReadings
    val selectedHomeId = uiState.selectedHomeId
    val selectedHome = homes.find { it.id == selectedHomeId }

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

        // Empty State: No homes added yet
        if (homes.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
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
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No Homes Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You haven't added any homes yet. Go to Settings to add your first home.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Home Selector Dropdown
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopStart)
            ) {
                // Trigger Button
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedHome?.name ?: homes.firstOrNull()?.name ?: "My Home",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Home",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dropdown Menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    homes.forEach { home ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = home.name,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            onClick = {
                                viewModel.selectHome(home.id)
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
                        onClick = { 
                            val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                            onMeterSelected(MeterType.GAS, homeId) 
                        },
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

            // Electricity Reading
            val electricityReading = latestReadings[MeterType.ELECTRICITY]
            MeterCard(
                meterType = MeterType.ELECTRICITY,
                lastReading = electricityReading?.value?.let { 
                    NumberFormat.getNumberInstance(Locale.US).format(it) 
                } ?: "-",
                lastReadingDate = electricityReading?.readingDate?.let { 
                    android.text.format.DateFormat.format("MMM dd", it).toString() 
                } ?: "-",
                consumptionString = "-", // TODO: Calculate consumption from previous reading
                isTrendingUp = false,
                onClick = { 
                    val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                    onMeterSelected(MeterType.ELECTRICITY, homeId) 
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gas Reading
            val gasReading = latestReadings[MeterType.GAS]
            MeterCard(
                meterType = MeterType.GAS,
                lastReading = gasReading?.value?.let { 
                    NumberFormat.getNumberInstance(Locale.US).format(it) 
                } ?: "-",
                lastReadingDate = gasReading?.readingDate?.let { 
                    android.text.format.DateFormat.format("MMM dd", it).toString() 
                } ?: "-",
                consumptionString = "-",
                isTrendingUp = false,
                onClick = { 
                    val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                    onMeterSelected(MeterType.GAS, homeId) 
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Water Reading
            val waterReading = latestReadings[MeterType.WATER]
            MeterCard(
                meterType = MeterType.WATER,
                lastReading = waterReading?.value?.let { 
                    NumberFormat.getNumberInstance(Locale.US).format(it) 
                } ?: "-",
                lastReadingDate = waterReading?.readingDate?.let { 
                    android.text.format.DateFormat.format("MMM dd", it).toString() 
                } ?: "-",
                consumptionString = "-",
                isTrendingUp = false,
                onClick = { 
                    val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                    onMeterSelected(MeterType.WATER, homeId) 
                }
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
}