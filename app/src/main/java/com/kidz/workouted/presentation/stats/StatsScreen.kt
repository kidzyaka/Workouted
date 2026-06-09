package com.kidz.workouted.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your progress for the last 30 days",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (val state = uiState) {
            is StatsUiState.Loading -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is StatsUiState.Success -> {
                ActivityChart(state.activityData)
                Spacer(modifier = Modifier.height(24.dp))
                BalanceChart(state.muscleBalance)
                Spacer(modifier = Modifier.height(24.dp))
                ProgressChart(state.progressData)
            }
            is StatsUiState.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ActivityChart(data: List<ActivityData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .fillMaxHeight(item.value)
                                .background(
                                    if (item.value > 0.6f) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.day, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceChart(balance: Map<String, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Radar Chart Placeholder with Canvas
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.size(180.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2
                    
                    // Draw polygons
                    drawCircle(color = primaryColor.copy(alpha = 0.1f), radius = radius, style = Fill)
                    drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = radius * 0.6f, style = Stroke(1f))
                    
                    // Simplified radar path
                    val path = Path().apply {
                        moveTo(center.x, center.y - radius * 0.8f)
                        lineTo(center.x + radius * 0.7f, center.y - radius * 0.3f)
                        lineTo(center.x + radius * 0.5f, center.y + radius * 0.6f)
                        lineTo(center.x - radius * 0.5f, center.y + radius * 0.6f)
                        lineTo(center.x - radius * 0.7f, center.y - radius * 0.3f)
                        close()
                    }
                    drawPath(path, color = primaryColor.copy(alpha = 0.4f), style = Fill)
                    drawPath(path, color = primaryColor, style = Stroke(4f))
                }
            }
        }
    }
}

@Composable
fun ProgressChart(data: List<ProgressData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Progress 1RM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Line Chart Placeholder
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val spacing = size.width / (data.size - 1)
                val maxVal = data.maxOf { it.value }
                val minVal = data.minOf { it.value }
                val range = maxVal - minVal
                
                val points = data.mapIndexed { index, item ->
                    Offset(
                        x = index * spacing,
                        y = size.height - ((item.value - minVal) / range) * size.height
                    )
                }
                
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = Color(0xFFE57373), // Coral/Red for progress
                        start = points[i],
                        end = points[i+1],
                        strokeWidth = 6f
                    )
                }
                
                points.forEach { point ->
                    drawCircle(color = Color.White, radius = 8f, center = point)
                    drawCircle(color = Color(0xFFE57373), radius = 4f, center = point)
                }
            }
        }
    }
}
