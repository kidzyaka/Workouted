package com.kidz.workouted.presentation.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.kidz.workouted.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExerciseScreen(
    viewModel: AddWorkoutViewModel,
    onBackClick: () -> Unit,
    onExerciseSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = remember(searchQuery, uiState.availableExercises) {
        uiState.availableExercises.filter { 
            it.exercise.name.contains(searchQuery, ignoreCase = true) 
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
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_exercises)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredExercises) { item ->
                    ListItem(
                        headlineContent = { 
                            Text(
                                item.exercise.name,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        supportingContent = { 
                            Text("${item.impacts.size} ${stringResource(R.string.target_muscles)}")
                        },
                        modifier = Modifier.clickable {
                            viewModel.addExercise(item.exercise)
                            onExerciseSelected()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
