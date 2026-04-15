package com.optimeter.app.presentation.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.optimeter.app.domain.model.MeterReading
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@Suppress("DEPRECATION", "DEPRECATION_ERROR")
@Composable
fun StatisticsTab(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHistory: (MeterType) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMeterType = uiState.selectedAnalyticsMeterType

    // Fetch fresh data when tab opens
    LaunchedEffect(uiState.selectedHomeId) {
        uiState.selectedHomeId?.let { homeId ->
            viewModel.fetchAllReadings(homeId)
        }
    }

    val meterTypes = listOf(
        MeterType.ELECTRICITY to "Electricity",
        MeterType.WATER to "Water",
        MeterType.GAS to "Gas"
    )

    // Filter and Sort Data Dynamically
    val allReadings = uiState.allReadings
    val filteredReadings = remember(allReadings, selectedMeterType) {
        allReadings
            .filter { it.type == selectedMeterType }
            .sortedBy { it.readingDate }
    }

    // Calculate Consumptions (differences between consecutive readings)
    val consumptionData = remember(filteredReadings) {
        if (filteredReadings.size >= 2) {
            filteredReadings.zipWithNext { prev, curr ->
                (curr.value - prev.value).toFloat().coerceAtLeast(0f)
            }.filterNot { it.isNaN() || it.isInfinite() }
        } else {
            emptyList()
        }
    }

    val rawAverage = if (consumptionData.isNotEmpty()) {
        consumptionData.average()
    } else if (filteredReadings.isNotEmpty()) {
        filteredReadings.last().value
    } else {
        0.0
    }
    val average = if (rawAverage.isNaN() || rawAverage.isInfinite()) 0.0 else rawAverage
    
    // Calculate Trend
    val trend = if (consumptionData.size >= 2) {
        val recent = consumptionData.last()
        val previous = consumptionData[consumptionData.size - 2]
        if (previous > 0f) ((recent - previous) / previous) * 100 else 0f
    } else {
        0f
    }

    val unit = if (selectedMeterType == MeterType.ELECTRICITY) "kWh" else "m³"
    val themeColor = when(selectedMeterType) {
        MeterType.ELECTRICITY -> Chart1
        MeterType.GAS -> Chart2
        MeterType.WATER -> Chart4
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text("Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Custom Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            meterTypes.forEach { (type, label) ->
                val isSelected = selectedMeterType == type
                val tabColor = when(type) {
                    MeterType.ELECTRICITY -> Chart1
                    MeterType.GAS -> Chart2
                    MeterType.WATER -> Chart4
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) tabColor else MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.selectAnalyticsMeterType(type) }
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
            // Average Card
            StatCard(
                title = "Average",
                value = average.toInt().toString(),
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
                    // Critical: Binary logic for Trend Color and Text
                    // trend <= 0.0 is GOOD (Color.Green)
                    // trend > 0.0 is BAD (Color.Red)
                    val isBad = trend > 0.0
                    val trendInt = trend.toInt()
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isBad) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null, 
                            modifier = Modifier.size(12.dp), 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    val trendText = when {
                        trendInt > 0 -> "+${trendInt}%"
                        trendInt < 0 -> "${trendInt}%"
                        else -> "0%"
                    }
                    
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (trend <= 0.0) Color(0xFF66BB6A) else Color.Red
                    )
                    Text("vs last", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val chunks = remember(filteredReadings) {
            if (filteredReadings.isEmpty()) emptyList()
            else filteredReadings.chunked(12)
        }
        var currentPageIndex by remember(chunks) {
            mutableIntStateOf(if (chunks.isNotEmpty()) chunks.size - 1 else 0)
        }
        val currentReadings = if (chunks.isNotEmpty() && currentPageIndex in chunks.indices) {
            chunks[currentPageIndex]
        } else {
            emptyList()
        }

        // Chart Area
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Meter Readings Over Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    
                    if (chunks.size > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                                enabled = currentPageIndex > 0,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Page")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Page ${currentPageIndex + 1}", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { if (currentPageIndex < chunks.size - 1) currentPageIndex++ },
                                enabled = currentPageIndex < chunks.size - 1,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Page")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                CustomLineChart(
                    readings = currentReadings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    lineColor = themeColor,
                    unit = unit,
                    startIndex = currentPageIndex * 12
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

    // Reading Cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Previous Reading",
            value = if (filteredReadings.size >= 2) filteredReadings[filteredReadings.size - 2].value.toInt().toString() else "—",
            subtitle = unit,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Latest Reading",
            value = if (filteredReadings.isNotEmpty()) filteredReadings.last().value.toInt().toString() else "—",
            subtitle = unit,
            modifier = Modifier.weight(1f)
        )
        val differenceValue = if (filteredReadings.size >= 2) {
            (filteredReadings.last().value - filteredReadings[filteredReadings.size - 2].value).toInt().toString()
        } else {
            "—"
        }
        StatCard(
            title = "Difference (Різниця)",
            value = differenceValue,
            subtitle = unit,
            valueColor = themeColor,
            modifier = Modifier.weight(1f)
        )
    }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onNavigateToHistory(selectedMeterType) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColor,
                contentColor = Color.White
            )
        ) {
            Text("View All Readings")
        }
    }
}

@Composable
fun CustomLineChart(
    readings: List<MeterReading>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    unit: String,
    startIndex: Int
) {
    if (readings.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var selectedIndex by remember(readings) { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            delay(3500)
            selectedIndex = null
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val axisLabelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val tooltipStyle = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
    
    Canvas(
        modifier = modifier.pointerInput(readings) {
            detectTapGestures { offset ->
                val leftPadding = 40.dp.toPx()
                val rightPadding = 20.dp.toPx()
                val graphWidth = size.width - leftPadding - rightPadding
                val stepX = if (readings.size > 1) graphWidth / (readings.size - 1) else graphWidth / 2f
                
                var closestIndex = -1
                var minDistance = Float.MAX_VALUE
                readings.forEachIndexed { index, _ ->
                    val xOffset = if (readings.size == 1) leftPadding + graphWidth / 2f else leftPadding + index * stepX
                    val dist = Math.abs(offset.x - xOffset)
                    if (dist < minDistance && dist < 40.dp.toPx()) {
                        minDistance = dist
                        closestIndex = index
                    }
                }
                if (closestIndex != -1) {
                    selectedIndex = closestIndex
                } else {
                    selectedIndex = null
                }
            }
        }
    ) {
        val leftPadding = 40.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val rightPadding = 20.dp.toPx()
        val topPadding = 20.dp.toPx()

        val graphWidth = size.width - leftPadding - rightPadding
        val graphHeight = size.height - topPadding - bottomPadding

        val minReading = readings.minOf { it.value.toFloat() }
        val maxReading = readings.maxOf { it.value.toFloat() }
        
        var graphMinY = minReading
        var graphMaxY = maxReading
        val range = graphMaxY - graphMinY
        
        if (range < 50f) {
            graphMinY = maxOf(0f, minReading - 20f)
            graphMaxY = maxReading + 20f
        } else {
            graphMinY = maxOf(0f, minReading - (range * 0.1f))
            graphMaxY = maxReading + (range * 0.1f)
        }
        
        val newRange = graphMaxY - graphMinY
        
        val intervals = 5
        val stepY = if (intervals > 1) newRange / (intervals - 1) else 0f
        val yAxisValues = List(intervals) { i -> (graphMinY + i * stepY).toInt() }

        // Draw horizontal guidelines and Y-axis labels
        yAxisValues.forEach { value ->
            val yRatio = if (newRange == 0f) 0.5f else (value.toFloat() - graphMinY) / newRange
            val yOffset = topPadding + graphHeight - (yRatio * graphHeight)
            
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(leftPadding, yOffset),
                end = Offset(size.width - rightPadding, yOffset),
                strokeWidth = 1.dp.toPx()
            )
            
            val textResult = textMeasurer.measure(value.toString(), axisLabelStyle)
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(leftPadding - textResult.size.width - 8.dp.toPx(), yOffset - textResult.size.height / 2f)
            )
        }

        val points = mutableListOf<Offset>()
        val stepX = if (readings.size > 1) graphWidth / (readings.size - 1) else graphWidth / 2f

        readings.forEachIndexed { index, reading ->
            val xOffset = if (readings.size == 1) leftPadding + graphWidth / 2f else leftPadding + index * stepX
            val yRatio = if (newRange == 0f) 0.5f else (reading.value.toFloat() - graphMinY) / newRange
            val yOffset = topPadding + graphHeight - (yRatio * graphHeight)
            points.add(Offset(xOffset, yOffset))
            
            // Draw X-axis label
            val label = (startIndex + index + 1).toString()
            val textResult = textMeasurer.measure(label, axisLabelStyle)
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(xOffset - textResult.size.width / 2f, size.height - bottomPadding + 8.dp.toPx())
            )
        }

        if (points.size > 1) {
            val path = Path()
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }

        selectedIndex?.let { index ->
            val point = points[index]
            val reading = readings[index]
            
            drawLine(
                color = lineColor.copy(alpha = 0.5f),
                start = Offset(point.x, topPadding),
                end = Offset(point.x, size.height - bottomPadding),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            
            val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(reading.readingDate))
            val text = "${reading.value.toInt()} $unit\n$dateStr"
            val tooltipResult = textMeasurer.measure(text, tooltipStyle)
            
            val tooltipPadding = 8.dp.toPx()
            val tooltipWidth = tooltipResult.size.width + tooltipPadding * 2
            val tooltipHeight = tooltipResult.size.height + tooltipPadding * 2
            
            var tooltipX = point.x - tooltipWidth / 2f
            // Adjust bounds
            if (tooltipX < leftPadding) tooltipX = leftPadding
            if (tooltipX + tooltipWidth > size.width - rightPadding) tooltipX = size.width - rightPadding - tooltipWidth
            
            val tooltipY = maxOf(0f, point.y - tooltipHeight - 8.dp.toPx())
            
            drawRoundRect(
                color = Color.DarkGray.copy(alpha = 0.9f),
                topLeft = Offset(tooltipX, tooltipY),
                size = Size(tooltipWidth, tooltipHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            drawText(
                textLayoutResult = tooltipResult,
                topLeft = Offset(tooltipX + tooltipPadding, tooltipY + tooltipPadding)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
