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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidz.workouted.R
import com.kidz.workouted.core.util.LocalizationUtil
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.presentation.components.StaggeredEntranceItem
import com.kidz.workouted.ui.theme.WorkoutedTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    StatsContent(uiState = uiState)
}

@Composable
fun StatsContent(
    uiState: StatsUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        StaggeredEntranceItem(index = 0) {
            Column {
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
            }
        }

        when (val state = uiState) {
            is StatsUiState.Loading -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is StatsUiState.Success -> {
                StaggeredEntranceItem(index = 1) {
                    ActivityChart(state.activityData)
                }
                Spacer(modifier = Modifier.height(24.dp))
                StaggeredEntranceItem(index = 2) {
                    BalanceChart(state.muscleBalance)
                }
                Spacer(modifier = Modifier.height(24.dp))
                StaggeredEntranceItem(index = 3) {
                    ProgressionScales(state.muscleProgression)
                }
                Spacer(modifier = Modifier.height(24.dp))
                StaggeredEntranceItem(index = 4) {
                    if (state.progressData.isNotEmpty()) {
                        ProgressChart(state.progressData)
                    } else {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.no_progress_data),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
fun ProgressionScales(progression: List<MuscleProgression>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.muscle_map),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            progression.forEach { item ->
                MuscleProgressBar(item.name, item.currentScore)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun MuscleProgressBar(nameKey: String, score: Float) {
    val ranks = Rank.entries.sortedBy { it.minScore }
    val maxRankScore = ranks.last().minScore.toFloat()
    val maxScaleScore = maxRankScore + 100f
    val progress = (score / maxScaleScore).coerceIn(0f, 1f)
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalizationUtil.getLocalizedName(context, nameKey), 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold
            )
            Text(
                text = score.toInt().toString(), 
                style = MaterialTheme.typography.labelMedium,
                color = primaryColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            val barHeight = 8.dp.toPx()
            val centerY = size.height / 2
            
            drawRoundRect(
                color = onSurface.copy(alpha = 0.1f),
                topLeft = Offset(0f, centerY - barHeight / 2),
                size = Size(size.width, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2)
            )
            
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(0f, centerY - barHeight / 2),
                size = Size(size.width * progress, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2)
            )
            
            ranks.forEach { rank ->
                val x = (rank.minScore / maxScaleScore) * size.width
                
                drawLine(
                    color = onSurface.copy(alpha = 0.3f),
                    start = Offset(x, centerY - barHeight),
                    end = Offset(x, centerY + barHeight),
                    strokeWidth = 2.dp.toPx()
                )
                
                drawCircle(
                    color = rank.color,
                    radius = 3.dp.toPx(),
                    center = Offset(x, centerY)
                )
            }
        }
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
                modifier = Modifier.fillMaxWidth().height(180.dp),
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
        val context = LocalContext.current
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.muscle_balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                val labels = balance.keys.toList()
                val values = balance.values.toList()
                val textMeasurer = rememberTextMeasurer()

                if (labels.isEmpty()) {
                    Text(stringResource(R.string.add_workouts_balance), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Canvas(modifier = Modifier.size(280.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 * 0.6f
                        val angleStep = (2 * Math.PI / labels.size).toFloat()

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

                        for (i in labels.indices) {
                            val angle = i * angleStep - Math.PI.toFloat() / 2
                            val labelRadius = radius * 1.25f
                            val x = center.x + labelRadius * cos(angle)
                            val y = center.y + labelRadius * sin(angle)
                            
                            val localizedLabel = LocalizationUtil.getLocalizedName(context, labels[i])
                            val textLayoutResult = textMeasurer.measure(
                                text = localizedLabel,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            )
                            
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(
                                    x = x - textLayoutResult.size.width / 2,
                                    y = y - textLayoutResult.size.height / 2
                                )
                            )
                            
                            drawLine(
                                color = primaryColor.copy(alpha = 0.1f),
                                start = center,
                                end = Offset(
                                    center.x + radius * cos(angle),
                                    center.y + radius * sin(angle)
                                ),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val valuePath = Path()
                        for (i in values.indices) {
                            val r = radius * values[i].coerceIn(0.1f, 1f)
                            val angle = i * angleStep - Math.PI.toFloat() / 2
                            val x = center.x + r * cos(angle)
                            val y = center.y + r * sin(angle)
                            if (i == 0) valuePath.moveTo(x, y) else valuePath.lineTo(x, y)
                            
                            drawCircle(primaryColor, radius = 3.dp.toPx(), center = Offset(x, y))
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
            Spacer(modifier = Modifier.height(20.dp))
            
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 16.dp)) {
                if (data.size < 2) {
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

@Preview(showBackground = true)
@Composable
fun StatsPreview() {
    WorkoutedTheme {
        StatsContent(
            uiState = StatsUiState.Success(
                activityData = listOf(
                    ActivityData("M", 0.4f),
                    ActivityData("T", 0.7f),
                    ActivityData("W", 0.2f),
                    ActivityData("T", 0.9f),
                    ActivityData("F", 0.5f),
                    ActivityData("S", 0.3f),
                    ActivityData("S", 0.1f)
                ),
                progressData = listOf(
                    ProgressData("Jun 1", 100f),
                    ProgressData("Jun 5", 110f),
                    ProgressData("Jun 10", 115f)
                ),
                muscleBalance = mapOf(
                    "group_chest" to 0.8f,
                    "group_back" to 0.6f,
                    "group_legs" to 0.9f,
                    "group_arms" to 0.4f,
                    "group_shoulders" to 0.7f,
                    "group_core" to 0.5f
                ),
                muscleProgression = listOf(
                    MuscleProgression("group_chest", 250f),
                    MuscleProgression("group_back", 180f),
                    MuscleProgression("group_legs", 450f)
                )
            )
        )
    }
}
