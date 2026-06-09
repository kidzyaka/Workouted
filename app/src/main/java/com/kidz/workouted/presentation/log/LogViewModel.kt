package com.kidz.workouted.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.domain.model.Workout
import com.kidz.workouted.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<LogUiState> = repository.getWorkouts()
        .map { workouts -> LogUiState.Success(workouts) }
        .onStart { LogUiState.Loading }
        .catch { e -> LogUiState.Error(e.message ?: "Unknown error") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LogUiState.Loading
        )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }
}
