package com.kidz.workouted.presentation.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidz.workouted.R
import com.kidz.workouted.core.util.LocalizationUtil
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.ui.theme.WorkoutedTheme
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun AddWorkoutScreen(
    viewModel: AddWorkoutViewModel,
    onAddExerciseClick: () -> Unit,
    onBackClick: () -> Unit,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AddWorkoutContent(
        uiState = uiState,
        onAddExerciseClick = onAddExerciseClick,
        onBackClick = onBackClick,
        onFinish = onFinish,
        onTimestampChange = { viewModel.setTimestamp(it) },
        onSaveWorkout = { viewModel.saveWorkout() },
        onAddSet = { viewModel.addSet(it) },
        onRemoveExercise = { viewModel.removeExercise(it) },
        onUpdateSet = { exIdx, setIdx, w, r -> viewModel.updateSet(exIdx, setIdx, w, r) },
        onRemoveSet = { exIdx, setIdx -> viewModel.removeSet(exIdx, setIdx) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutContent(
    uiState: AddWorkoutUiState,
    onAddExerciseClick: () -> Unit,
    onBackClick: () -> Unit,
    onFinish: () -> Unit,
    onTimestampChange: (Long) -> Unit,
    onSaveWorkout: () -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateSet: (Int, Int, String, String) -> Unit,
    onRemoveSet: (Int, Int) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.timestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onTimestampChange(it) }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.finish))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                title = {
                    Column(modifier = Modifier.clickable { showDatePicker = true }) {
                        Text(stringResource(R.string.log_workout), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text(
                            text = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(uiState.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onSaveWorkout,
                        enabled = uiState.exercises.isNotEmpty() && !uiState.isSaving,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.finish))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExerciseClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_exercise)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        if (uiState.exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.start_adding),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.exercises) { exerciseIndex, activeExercise ->
                    ExerciseCard(
                        activeExercise = activeExercise,
                        onAddSet = { onAddSet(exerciseIndex) },
                        onRemoveExercise = { onRemoveExercise(exerciseIndex) },
                        onUpdateSet = { setIndex, weight, reps ->
                            onUpdateSet(exerciseIndex, setIndex, weight, reps)
                        },
                        onRemoveSet = { setIndex ->
                            onRemoveSet(exerciseIndex, setIndex)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    activeExercise: ActiveExercise,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onUpdateSet: (Int, String, String) -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalizationUtil.getLocalizedName(context, activeExercise.exercise.name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemoveExercise) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Remove Exercise",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Set Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.set_label), 
                    modifier = Modifier.width(40.dp), 
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.weight_kg), 
                    modifier = Modifier.weight(1f), 
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.reps), 
                    modifier = Modifier.weight(1f), 
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // Matching IconButton width
            }

            activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (setIndex + 1).toString(),
                        modifier = Modifier.width(40.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = activeSet.weight,
                        onValueChange = { onUpdateSet(setIndex, it, activeSet.reps) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = activeSet.reps,
                        onValueChange = { onUpdateSet(setIndex, activeSet.weight, it) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )

                    IconButton(
                        onClick = { onRemoveSet(setIndex) },
                        enabled = activeExercise.sets.size > 1,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove Set")
                    }
                }
            }

            Button(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_set))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddWorkoutPreview() {
    WorkoutedTheme {
        AddWorkoutContent(
            uiState = AddWorkoutUiState(
                exercises = listOf(
                    ActiveExercise(
                        exercise = ExerciseEntity(name = "ex_bench_press_classic", isWeightBased = true, maxWeightReference = 100.0),
                        sets = listOf(ActiveSet("60", "10"), ActiveSet("65", "8"))
                    )
                )
            ),
            onAddExerciseClick = {},
            onBackClick = {},
            onFinish = {},
            onTimestampChange = {},
            onSaveWorkout = {},
            onAddSet = {},
            onRemoveExercise = {},
            onUpdateSet = { _, _, _, _ -> },
            onRemoveSet = { _, _ -> }
        )
    }
}
