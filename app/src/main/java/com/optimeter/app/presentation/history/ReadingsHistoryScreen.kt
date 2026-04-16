package com.optimeter.app.presentation.history

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.dashboard.tabs.HomeViewModel
import com.optimeter.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsHistoryScreen(
    initialMeterType: MeterType? = null,
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMeterType by remember { mutableStateOf(initialMeterType ?: MeterType.WATER) }
    
    LaunchedEffect(initialMeterType) {
        initialMeterType?.let {
            selectedMeterType = it
        }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var readingToDelete by remember { mutableStateOf<com.optimeter.app.domain.model.MeterReading?>(null) }

    // Fetch fresh data when screen opens
    LaunchedEffect(uiState.selectedHomeId) {
        uiState.selectedHomeId?.let { homeId ->
            viewModel.fetchAllReadings(homeId)
        }
    }

    val meterTypes = listOf(
        MeterType.WATER to "Water",
        MeterType.ELECTRICITY to "Electricity",
        MeterType.GAS to "Gas"
    )

    // Filter and Sort Data Dynamically (Descending)
    val allReadings = uiState.allReadings
    val filteredReadings = remember(allReadings, selectedMeterType) {
        allReadings
            .filter { it.type == selectedMeterType }
            .sortedByDescending { it.readingDate }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Custom Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                meterTypes.forEach { (type, label) ->
                    val isSelected = selectedMeterType == type
                    val tabColor = when (type) {
                        MeterType.ELECTRICITY -> Chart1
                        MeterType.GAS -> Chart2
                        MeterType.WATER -> Chart4
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) tabColor else MaterialTheme.colorScheme.surface)
                            .clickable { selectedMeterType = type }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredReadings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No readings found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReadings) { reading ->
                        val unit = if (reading.type == MeterType.ELECTRICITY) "kWh" else "m³"
                        val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
                        val dateStr = dateFormat.format(Date(reading.readingDate))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${reading.value.toInt()} $unit",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        readingToDelete = reading
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp))
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && readingToDelete != null) {
        val reading = readingToDelete!!
        val unit = if (reading.type == MeterType.ELECTRICITY) "kWh" else "m³"
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                readingToDelete = null
            },
            title = { Text("Delete Reading?") },
            text = { Text("Are you sure you want to delete the reading of ${reading.value} $unit? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val homeId = uiState.selectedHomeId
                        if (homeId != null) {
                            viewModel.deleteReading(homeId, reading.id)
                        }
                        showDeleteDialog = false
                        readingToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        readingToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
