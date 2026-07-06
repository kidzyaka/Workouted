package com.kidz.workouted.presentation.log

import com.kidz.workouted.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidz.workouted.domain.model.Workout
import com.kidz.workouted.ui.theme.WorkoutedTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onWorkoutClick: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LogContent(
        uiState = uiState,
        onWorkoutClick = onWorkoutClick,
        onDeleteWorkout = { viewModel.deleteWorkout(it) }
    )
}

@Composable
fun LogContent(
    uiState: LogUiState,
    onWorkoutClick: (Long) -> Unit,
    onDeleteWorkout: (Workout) -> Unit
) {
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }

    if (workoutToDelete != null) {
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            title = { Text(stringResource(R.string.delete_workout)) },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        workoutToDelete?.let { onDeleteWorkout(it) }
                        workoutToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.workout_log),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (uiState) {
            is LogUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LogUiState.Success -> {
                if (uiState.workouts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_workouts))
                    }
                } else {
                    val locale = LocalConfiguration.current.locales[0]
                    val groupedWorkouts = remember(uiState.workouts, locale) {
                        uiState.workouts.groupBy { workout ->
                            val cal = Calendar.getInstance().apply { timeInMillis = workout.timestamp }
                            SimpleDateFormat("MMMM yyyy", locale).format(cal.time)
                        }
                    }
                    val months = groupedWorkouts.keys.toList()
                    var selectedMonthIndex by remember(months) { mutableIntStateOf(0) }
                    
                    // Safety check if index out of bounds after deletion
                    val safeIndex = selectedMonthIndex.coerceIn(0, months.size - 1)
                    val currentMonth = months.getOrNull(safeIndex)
                    val workoutsInMonth = currentMonth?.let { groupedWorkouts[it] } ?: emptyList()

                    ScrollableTabRow(
                        selectedTabIndex = safeIndex,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = { tabPositions ->
                            if (safeIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        months.forEachIndexed { index, month ->
                            Tab(
                                selected = safeIndex == index,
                                onClick = { selectedMonthIndex = index },
                                text = {
                                    Text(
                                        text = month.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (safeIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = workoutsInMonth,
                            key = { it.id }
                        ) { workout ->
                            WorkoutItem(
                                workout = workout,
                                onClick = { onWorkoutClick(workout.id) },
                                onDelete = { workoutToDelete = workout }
                            )
                        }
                    }
                }
            }
            is LogUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutItem(
    workout: Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val swipeToDismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = swipeToDismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (swipeToDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.errorContainer
            } else Color.Transparent

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                val locale = LocalConfiguration.current.locales[0]
                Column {
                    Text(
                        text = SimpleDateFormat("EEEE, HH:mm", locale)
                            .format(Date(workout.timestamp)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${workout.exercisesCount} ${stringResource(R.string.exercises)} • ${workout.totalVolume.toInt()} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogPreview() {
    WorkoutedTheme {
        LogContent(
            uiState = LogUiState.Success(
                workouts = listOf(
                    Workout(1, System.currentTimeMillis(), 1500.0, 3),
                    Workout(2, System.currentTimeMillis() - 86400000 * 35, 800.0, 2)
                )
            ),
            onWorkoutClick = {},
            onDeleteWorkout = {}
        )
    }
}
