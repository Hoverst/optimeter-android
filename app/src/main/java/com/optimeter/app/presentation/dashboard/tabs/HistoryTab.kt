package com.optimeter.app.presentation.dashboard.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optimeter.app.R
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.domain.model.Reading
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryTab(
    onReadingClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<MeterType?>(null) }

    val readings = listOf(
        Reading("1", MeterType.GAS, 1250.0, System.currentTimeMillis(), false),
        Reading("2", MeterType.WATER, 450.0, System.currentTimeMillis() - 86400000, true)
    )

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedFilter?.ordinal?.plus(1) ?: 0,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                text = { Text(stringResource(R.string.history)) }
            )
            MeterType.values().forEach { type ->
                Tab(
                    selected = selectedFilter == type,
                    onClick = { selectedFilter = type },
                    text = {
                        Text(
                            when (type) {
                                MeterType.GAS -> stringResource(R.string.gas_meter)
                                MeterType.WATER -> stringResource(R.string.water_meter)
                                MeterType.ELECTRICITY -> stringResource(R.string.electricity_meter)
                            }
                        )
                    }
                )
            }
        }

        val filtered = if (selectedFilter == null) readings else readings.filter { it.meterType == selectedFilter }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_readings))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { reading ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onReadingClick(reading.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = when (reading.meterType) {
                                        MeterType.GAS -> stringResource(R.string.gas_meter)
                                        MeterType.WATER -> stringResource(R.string.water_meter)
                                        MeterType.ELECTRICITY -> stringResource(R.string.electricity_meter)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = dateFormat.format(Date(reading.timestamp)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (reading.isManualEntry) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) { Text(stringResource(R.string.enter_manually)) }
                                }
                            }
                            Text(
                                text = reading.value.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
