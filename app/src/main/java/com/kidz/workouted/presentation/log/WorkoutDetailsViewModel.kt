package com.kidz.workouted.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutDetailsUiState(
    val timestamp: Long = 0,
    val exercises: List<Pair<String, List<SetEntity>>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutDetailsUiState())
    val uiState: StateFlow<WorkoutDetailsUiState> = _uiState.asStateFlow()

    fun loadWorkout(workoutId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val workout = repository.getWorkoutById(workoutId)
                val sets = repository.getSetsForWorkout(workoutId)
                val availableExercises = repository.getExercisesWithImpacts().first()
                val exerciseMap = availableExercises.associate { it.exercise.id to it.exercise.name }

                val groupedSets = sets.groupBy { it.exerciseId }
                    .map { (exerciseId, exerciseSets) ->
                        (exerciseMap[exerciseId] ?: "Unknown Exercise") to exerciseSets
                    }

                _uiState.update { it.copy(
                    timestamp = workout?.timestamp ?: 0L,
                    exercises = groupedSets,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
