package com.kidz.workouted.presentation.log

import android.util.Log
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
    val workoutId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
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
        Log.d("AddWorkoutVM", "Initializing AddWorkoutViewModel")
        repository.getExercisesWithImpacts()
            .onEach { exercises ->
                Log.d("AddWorkoutVM", "Loaded ${exercises.size} exercises into ViewModel state.")
                _uiState.update { it.copy(availableExercises = exercises) }
            }
            .launchIn(viewModelScope)
    }

    fun loadWorkout(id: Long) {
        if (id == 0L || _uiState.value.workoutId == id) return
        
        viewModelScope.launch {
            val workout = repository.getWorkoutById(id) ?: return@launch
            val sets = repository.getSetsForWorkout(id)
            
            // Wait for available exercises to be loaded if they aren't yet
            val available = _uiState.value.availableExercises.ifEmpty {
                repository.getExercisesWithImpacts().first()
            }
            
            val exerciseMap = available.associateBy { it.exercise.id }
            
            val activeExercises = sets.groupBy { it.exerciseId }.map { (exerciseId, exerciseSets) ->
                val exercise = exerciseMap[exerciseId]?.exercise ?: return@map null
                ActiveExercise(
                    exercise = exercise,
                    sets = exerciseSets.map { ActiveSet(it.weight.toString(), it.reps.toString()) }
                )
            }.filterNotNull()

            _uiState.update { it.copy(
                workoutId = id,
                timestamp = workout.timestamp,
                exercises = activeExercises
            ) }
        }
    }

    fun setTimestamp(timestamp: Long) {
        _uiState.update { it.copy(timestamp = timestamp) }
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
            
            val setsToSave = state.exercises.flatMap { activeExercise ->
                activeExercise.sets.mapNotNull { activeSet ->
                    val weight = activeSet.weight.toDoubleOrNull() ?: 0.0
                    val reps = activeSet.reps.toIntOrNull() ?: 0
                    if (reps > 0) {
                        SetEntity(
                            workoutId = state.workoutId,
                            exerciseId = activeExercise.exercise.id,
                            weight = weight,
                            reps = reps
                        )
                    } else null
                }
            }

            if (setsToSave.isNotEmpty()) {
                repository.saveWorkout(state.timestamp, setsToSave, state.workoutId)
            }
            
            _uiState.update { it.copy(isSaving = false, isFinished = true) }
        }
    }
}
