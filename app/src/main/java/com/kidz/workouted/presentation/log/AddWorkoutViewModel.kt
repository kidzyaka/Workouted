package com.kidz.workouted.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.dao.ExerciseWithImpacts
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveExercise(
    val exercise: ExerciseEntity,
    val sets: List<ActiveSet> = listOf(ActiveSet())
)

data class ActiveSet(
    val weight: String = "",
    val reps: String = ""
)

data class AddWorkoutUiState(
    val exercises: List<ActiveExercise> = emptyList(),
    val availableExercises: List<ExerciseWithImpacts> = emptyList(),
    val isSaving: Boolean = false,
    val isFinished: Boolean = false
)

@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWorkoutUiState())
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    init {
        repository.getExercisesWithImpacts()
            .onEach { exercises ->
                _uiState.update { it.copy(availableExercises = exercises) }
            }
            .launchIn(viewModelScope)
    }

    fun addExercise(exercise: ExerciseEntity) {
        _uiState.update { state ->
            val newList = state.exercises + ActiveExercise(exercise)
            state.copy(exercises = newList)
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList().apply { removeAt(index) }
            state.copy(exercises = newList)
        }
    }

    fun addSet(exerciseIndex: Int) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList()
            val exercise = newList[exerciseIndex]
            newList[exerciseIndex] = exercise.copy(sets = exercise.sets + ActiveSet())
            state.copy(exercises = newList)
        }
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: String, reps: String) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList()
            val exercise = newList[exerciseIndex]
            val newSets = exercise.sets.toMutableList()
            newSets[setIndex] = ActiveSet(weight, reps)
            newList[exerciseIndex] = exercise.copy(sets = newSets)
            state.copy(exercises = newList)
        }
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList()
            val exercise = newList[exerciseIndex]
            if (exercise.sets.size > 1) {
                val newSets = exercise.sets.toMutableList().apply { removeAt(setIndex) }
                newList[exerciseIndex] = exercise.copy(sets = newSets)
                state.copy(exercises = newList)
            } else {
                state
            }
        }
    }

    fun saveWorkout() {
        val state = _uiState.value
        if (state.exercises.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val timestamp = System.currentTimeMillis()
            val setsToSave = state.exercises.flatMap { activeExercise ->
                activeExercise.sets.mapNotNull { activeSet ->
                    val weight = activeSet.weight.toDoubleOrNull() ?: 0.0
                    val reps = activeSet.reps.toIntOrNull() ?: 0
                    if (reps > 0) {
                        SetEntity(
                            workoutId = 0, // Will be set by DAO transaction
                            exerciseId = activeExercise.exercise.id,
                            weight = weight,
                            reps = reps
                        )
                    } else null
                }
            }

            if (setsToSave.isNotEmpty()) {
                repository.saveWorkout(timestamp, setsToSave)
            }
            
            _uiState.update { it.copy(isSaving = false, isFinished = true) }
        }
    }
}
