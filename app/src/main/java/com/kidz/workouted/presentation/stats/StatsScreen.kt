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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

import com.kidz.workouted.R
import androidx.compose.ui.res.stringResource

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
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.progress_30_days),
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
                if (state.progressData.isNotEmpty()) {
                    ProgressChart(state.progressData)
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_progress_data), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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
                text = stringResource(R.string.workload),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp), // Increased height for labels
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .fillMaxHeight(item.value.coerceIn(0.05f, 1f))
                                    .background(
                                        if (item.value > 0.1f) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
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
                text = stringResource(R.string.muscle_balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val labels = balance.keys.toList()
                val values = balance.values.toList()

                if (labels.isEmpty()) {
                    Text(stringResource(R.string.add_workouts_balance), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // ... (rest of Canvas logic remains the same)
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 * 0.8f
                        val angleStep = (2 * Math.PI / labels.size).toFloat()

                        // Draw web
                        for (i in 1..5) {
                            val r = radius * (i / 5f)
                            val path = Path()
                            for (j in labels.indices) {
                                val angle = j * angleStep - Math.PI.toFloat() / 2
                                val x = center.x + r * cos(angle)
                                val y = center.y + r * sin(angle)
                                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            path.close()
                            drawPath(path, primaryColor.copy(alpha = 0.1f), style = Stroke(1.dp.toPx()))
                        }

                        // Draw values
                        val valuePath = Path()
                        for (i in values.indices) {
                            val r = radius * values[i].coerceIn(0.1f, 1f)
                            val angle = i * angleStep - Math.PI.toFloat() / 2
                            val x = center.x + r * cos(angle)
                            val y = center.y + r * sin(angle)
                            if (i == 0) valuePath.moveTo(x, y) else valuePath.lineTo(x, y)
                            
                            // Draw point
                            drawCircle(primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
                        }
                        if (values.isNotEmpty()) valuePath.close()
                        
                        drawPath(valuePath, primaryColor.copy(alpha = 0.3f), style = Fill)
                        drawPath(valuePath, primaryColor, style = Stroke(2.dp.toPx()))
                    }
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
                text = stringResource(R.string.strength_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // ...
            Spacer(modifier = Modifier.height(20.dp))
            
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 16.dp)) {
                if (data.size < 2) {
                    // Not enough points for a line, just draw a circle
                    val valY = size.height / 2
                    drawCircle(Color(0xFFE57373), radius = 6.dp.toPx(), center = Offset(size.width / 2, valY))
                } else {
                    val spacing = size.width / (data.size - 1)
                    val maxVal = data.maxOf { it.value }.coerceAtLeast(1f)
                    val minVal = data.minOf { it.value } * 0.9f
                    val range = (maxVal - minVal).coerceAtLeast(1f)
                    
                    val points = data.mapIndexed { index, item ->
                        Offset(
                            x = index * spacing,
                            y = size.height - ((item.value - minVal) / range) * size.height
                        )
                    }
                    
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = Color(0xFFE57373),
                            start = points[i],
                            end = points[i+1],
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    
                    points.forEach { point ->
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = point)
                        drawCircle(Color(0xFFE57373), radius = 2.dp.toPx(), center = point)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { 
                    Text(it.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
