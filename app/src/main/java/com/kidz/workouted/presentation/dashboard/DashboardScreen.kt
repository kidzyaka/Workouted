package com.kidz.workouted.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kidz.workouted.R
import com.kidz.workouted.core.util.LocalizationUtil
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.presentation.components.MuscleBadge
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(uiState = uiState)
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.muscle_map),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.progress_by_muscle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Muscle Map with Body Silhouette
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), MaterialTheme.shapes.extraLarge)
            )

            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.body_silhouette),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(),
                alpha = 0.5f,
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            if (uiState is DashboardUiState.Success) {
                Box(Modifier.fillMaxSize()) {
                    // BACK (Center, high)
                    MuscleBadge(
                        LocalizationUtil.getLocalizedName(context, "group_back"), 
                        uiState.muscleGroupRanks["group_back"] ?: Rank.WOOD,
                        Modifier.align(BiasAlignment(0f, -0.7f))
                    )
                    // SHOULDERS (Sides)
                    MuscleBadge(
                        LocalizationUtil.getLocalizedName(context, "group_shoulders"), 
                        uiState.muscleGroupRanks["group_shoulders"] ?: Rank.WOOD,
                        Modifier.align(BiasAlignment(0.20f, -0.60f))
                    )
                    // CHEST (Center, under Back)
                    MuscleBadge(
                        LocalizationUtil.getLocalizedName(context, "group_chest"), 
                        uiState.muscleGroupRanks["group_chest"] ?: Rank.WOOD,
                        Modifier.align(BiasAlignment(-0.1f, -0.55f))
                    )
                    // ARMS (Left side)
                    MuscleBadge(
                        LocalizationUtil.getLocalizedName(context, "group_arms"), 
                        uiState.muscleGroupRanks["group_arms"] ?: Rank.WOOD,
                        Modifier.align(BiasAlignment(-0.5f, -0.15f))
                    )
                    // CORE
                    MuscleBadge(
                        LocalizationUtil.getLocalizedName(context, "group_core"), 
                        uiState.muscleGroupRanks["group_core"] ?: Rank.WOOD,
                        Modifier.align(BiasAlignment(0f, -0.20f))
                    )
                    // LEGS (Lower body)
                    MuscleBadge(
                        LocalizationUtil.getLocalizedName(context, "group_legs"), 
                        uiState.muscleGroupRanks["group_legs"] ?: Rank.WOOD,
                        Modifier.align(BiasAlignment(0f, 0.6f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (uiState) {
            is DashboardUiState.Success -> {
                WorkoutCalendar(uiState.workoutDates)
            }
            is DashboardUiState.Loading -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
    }
}

@Composable
fun WorkoutCalendar(workoutDates: Set<Long>) {
    val currentMonth = remember { YearMonth.now() }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value - 1
    
    val workoutLocalDates = remember(workoutDates) {
        workoutDates.map { 
            java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() 
        }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
            .padding(16.dp)
    ) {
        Text(
            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + " " + currentMonth.year,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val rows = (daysInMonth + firstDayOfMonth + 6) / 7
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - (firstDayOfMonth - 1)
                    if (dayNum in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNum)
                        val hasWorkout = workoutLocalDates.contains(date)
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasWorkout -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (hasWorkout || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    hasWorkout -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
