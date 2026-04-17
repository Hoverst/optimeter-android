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
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.dashboard.components.MeterCard
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.optimeter.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    onMeterSelected: (MeterType, String?) -> Unit,
    onAddNewReadingCamera: (String?) -> Unit,
    onAddNewReadingGallery: (String?) -> Unit,
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
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = stringResource(R.string.homes_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

            Spacer(modifier = Modifier.height(16.dp))

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
                        stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                            onAddNewReadingCamera(homeId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.add_new_reading_button), fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                            onAddNewReadingGallery(homeId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.choose_from_gallery), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.current_readings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Formatter for readings: whole numbers with space as thousands separator
            val formatReading = { value: Double ->
                String.format(Locale.US, "%,d", value.toLong()).replace(',', ' ')
            }

            val calculateTrend = { type: MeterType ->
                val typeReadings = uiState.allReadings
                    .filter { it.type == type }
                    .sortedBy { it.readingDate }
                
                val consumptionData = if (typeReadings.size >= 2) {
                    typeReadings.zipWithNext { prev, curr ->
                        (curr.value - prev.value).toFloat().coerceAtLeast(0f)
                    }
                } else emptyList()

                if (consumptionData.size >= 2) {
                    val recent = consumptionData.last()
                    val previous = consumptionData[consumptionData.size - 2]
                    if (previous > 0f) ((recent - previous) / previous) * 100.0 else 0.0
                } else {
                    null
                }
            }

            val formatTrend = { trendVal: Double? ->
                if (trendVal == null) null
                else {
                    val trendInt = trendVal.toInt()
                    when {
                        trendInt > 0 -> "+$trendInt%"
                        trendInt < 0 -> "$trendInt%"
                        else -> "0%"
                    }
                }
            }

            // Water Reading
            val waterReading = latestReadings[MeterType.WATER]
            val waterTrend = calculateTrend(MeterType.WATER)
            MeterCard(
                meterType = MeterType.WATER,
                lastReading = waterReading?.value?.let { formatReading(it) } ?: "-",
                lastReadingDate = waterReading?.readingDate?.let { 
                    android.text.format.DateFormat.format("MMM dd", it).toString() 
                } ?: "-",
                consumptionString = formatTrend(waterTrend),
                isTrendingUp = waterTrend?.let { it > 0.0 },
                onClick = { 
                    val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                    onMeterSelected(MeterType.WATER, homeId) 
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Electricity Reading
            val electricityReading = latestReadings[MeterType.ELECTRICITY]
            val electricityTrend = calculateTrend(MeterType.ELECTRICITY)
            MeterCard(
                meterType = MeterType.ELECTRICITY,
                lastReading = electricityReading?.value?.let { formatReading(it) } ?: "-",
                lastReadingDate = electricityReading?.readingDate?.let { 
                    android.text.format.DateFormat.format("MMM dd", it).toString() 
                } ?: "-",
                consumptionString = formatTrend(electricityTrend),
                isTrendingUp = electricityTrend?.let { it > 0.0 },
                onClick = { 
                    val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                    onMeterSelected(MeterType.ELECTRICITY, homeId) 
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gas Reading
            val gasReading = latestReadings[MeterType.GAS]
            val gasTrend = calculateTrend(MeterType.GAS)
            MeterCard(
                meterType = MeterType.GAS,
                lastReading = gasReading?.value?.let { formatReading(it) } ?: "-",
                lastReadingDate = gasReading?.readingDate?.let { 
                    android.text.format.DateFormat.format("MMM dd", it).toString() 
                } ?: "-",
                consumptionString = formatTrend(gasTrend),
                isTrendingUp = gasTrend?.let { it > 0.0 },
                onClick = { 
                    val homeId = selectedHomeId ?: homes.firstOrNull()?.id
                    onMeterSelected(MeterType.GAS, homeId) 
                }
            )
            
        }
    }
}
