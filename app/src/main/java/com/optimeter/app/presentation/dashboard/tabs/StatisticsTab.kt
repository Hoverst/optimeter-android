package com.optimeter.app.presentation.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@Composable
fun StatisticsTab() {
    var selectedMeterType by remember { mutableStateOf(MeterType.GAS) }

    val meterTypes = listOf(
        MeterType.ELECTRICITY to "Electricity",
        MeterType.GAS to "Gas",
        MeterType.WATER to "Water"
    )

    // Mock Data based on React prototype
    val consumptionData = when (selectedMeterType) {
        MeterType.GAS -> listOf(35f, 40f, 38f, 70f, 42f, 39f)
        MeterType.WATER -> listOf(12f, 15f, 14f, 13f, 16f, 14f)
        MeterType.ELECTRICITY -> listOf(150f, 160f, 155f, 170f, 300f, 165f)
    }

    val total = consumptionData.sum()
    val average = consumptionData.average()
    
    // Calculate Trend
    val recent = consumptionData.last()
    val previous = consumptionData[consumptionData.size - 2]
    val trend = ((recent - previous) / previous) * 100

    val unit = if (selectedMeterType == MeterType.ELECTRICITY) "kWh" else "m³"
    val themeColor = when(selectedMeterType) {
        MeterType.ELECTRICITY -> Chart1
        MeterType.GAS -> Chart2
        MeterType.WATER -> Chart4
    }

    val chartEntryModelProducer = remember(consumptionData) {
        ChartEntryModelProducer(
            consumptionData.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text("Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("Track your utility consumption", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Custom Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            meterTypes.forEach { (type, label) ->
                val isSelected = selectedMeterType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Chart1 else MaterialTheme.colorScheme.surface)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Stat Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Card
            StatCard(
                title = "Total",
                value = String.format("%.1f", total),
                subtitle = unit,
                modifier = Modifier.weight(1f)
            )
            
            // Average Card
            StatCard(
                title = "Average",
                value = String.format("%.1f", average),
                subtitle = "per period",
                modifier = Modifier.weight(1f)
            )
            
            // Trend Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f) // Ensure equal width
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val isUp = trend >= 0
                    Text(
                        text = "${if (isUp) "+" else ""}${String.format("%.1f", trend)}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUp) Chart5 else Chart2
                    )
                    Text("vs last", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Area
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Meter Readings Over Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))

                ProvideChartStyle {
                    Chart(
                        chart = lineChart(),
                        chartModelProducer = chartEntryModelProducer,
                        startAxis = rememberStartAxis(
                            label = com.patrykandpatrick.vico.compose.axis.axisLabelComponent(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            axis = null,
                            tick = null,
                            guideline = com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent(
                                color = MaterialTheme.colorScheme.outline,
                                strokeWidth = 1.dp
                            ) // Remove heavy grid lines, replace with subtle outline color
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = com.patrykandpatrick.vico.compose.axis.axisLabelComponent(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            guideline = null
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
