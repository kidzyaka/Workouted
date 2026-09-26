package com.kidz.workouted.presentation.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kidz.workouted.R
import com.kidz.workouted.core.util.LocalizationUtil
import com.kidz.workouted.data.local.dao.ExerciseWithImpacts
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.ui.theme.WorkoutedTheme

@Composable
fun SelectExerciseScreen(
    viewModel: AddWorkoutViewModel,
    onBackClick: () -> Unit,
    onExerciseSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    SelectExerciseContent(
        availableExercises = uiState.availableExercises,
        onBackClick = onBackClick,
        onExerciseSelected = { exercise ->
            viewModel.addExercise(exercise)
            onExerciseSelected()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExerciseContent(
    availableExercises: List<ExerciseWithImpacts>,
    onBackClick: () -> Unit,
    onExerciseSelected: (ExerciseEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val uniqueGroups = remember(availableExercises) {
        availableExercises.flatMap { it.impacts.map { imp -> imp.muscleId } }.distinct().sorted()
    }
    var selectedGroup by remember { mutableStateOf<String?>(null) }

    val filteredExercises = remember(searchQuery, selectedGroup, availableExercises, context) {
        availableExercises.filter { item ->
            val localizedName = LocalizationUtil.getLocalizedName(context, item.exercise.name)
            val matchesSearch = localizedName.contains(searchQuery, ignoreCase = true) 
            val matchesGroup = selectedGroup == null || item.impacts.any { it.muscleId == selectedGroup }
            matchesSearch && matchesGroup
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_exercise)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                placeholder = { Text(stringResource(R.string.search_exercises)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true
            )

            if (uniqueGroups.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uniqueGroups) { groupId ->
                        FilterChip(
                            selected = selectedGroup == groupId,
                            onClick = { 
                                selectedGroup = if (selectedGroup == groupId) null else groupId 
                            },
                            label = { Text(LocalizationUtil.getLocalizedName(context, groupId)) },
                            leadingIcon = if (selectedGroup == groupId) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredExercises) { item ->
                    ListItem(
                        headlineContent = { 
                            Text(
                                LocalizationUtil.getLocalizedName(context, item.exercise.name),
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        supportingContent = { 
                            Text("${item.impacts.size} ${stringResource(R.string.target_muscles)}")
                        },
                        modifier = Modifier.clickable {
                            onExerciseSelected(item.exercise)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectExercisePreview() {
    WorkoutedTheme {
        SelectExerciseContent(
            availableExercises = listOf(
                ExerciseWithImpacts(
                    exercise = ExerciseEntity(name = "ex_bench_press_classic", isWeightBased = true, maxWeightReference = 100.0),
                    impacts = emptyList()
                ),
                ExerciseWithImpacts(
                    exercise = ExerciseEntity(name = "ex_squats", isWeightBased = true, maxWeightReference = 150.0),
                    impacts = emptyList()
                )
            ),
            onBackClick = {},
            onExerciseSelected = {}
        )
    }
}
