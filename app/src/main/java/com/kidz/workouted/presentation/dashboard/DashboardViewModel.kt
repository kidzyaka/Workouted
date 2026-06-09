package com.kidz.workouted.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import com.kidz.workouted.domain.usecase.AggregateGroupRatingUseCase
import com.kidz.workouted.domain.usecase.CalculateMuscleRatingUseCase
import com.kidz.workouted.domain.usecase.CalculateSetEffortUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val calculateSetEffort: CalculateSetEffortUseCase,
    private val calculateMuscleRating: CalculateMuscleRatingUseCase,
    private val aggregateGroupRating: AggregateGroupRatingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        combine(
            workoutDao.getAllWorkouts(),
            workoutDao.getAllSets(),
            workoutDao.getExercisesWithMuscleImpacts(),
            workoutDao.getMuscleGroupsWithMuscles(),
            preferencesRepository.userHeightCm
        ) { workouts, sets, exercises, groups, height ->
            
            val muscleRatings = mutableMapOf<Long, Double>()
            val exerciseMap = exercises.associateBy { it.exercise.id }
            
            Log.d("DashboardVM", "Processing ${sets.size} sets for ${workouts.size} workouts. Height: $height")
            
            sets.forEach { set ->
                val exerciseWithImpacts = exerciseMap[set.exerciseId]
                if (exerciseWithImpacts != null) {
                    val baseEffort = calculateSetEffort(
                        weight = set.weight,
                        reps = set.reps,
                        maxWeightReference = exerciseWithImpacts.exercise.maxWeightReference,
                        userHeightCm = height
                    )
                    
                    exerciseWithImpacts.impacts.forEach { impact ->
                        val points = calculateMuscleRating(baseEffort, impact.impactCoefficient)
                        muscleRatings[impact.muscleId] = (muscleRatings[impact.muscleId] ?: 0.0) + points
                    }
                }
            }
            
            Log.d("DashboardVM", "Muscle Ratings: $muscleRatings")
            
            val groupRanks = groups.associate { groupWithMuscles ->
                val groupMuscleRatings = groupWithMuscles.muscles.associate { it.id to (muscleRatings[it.id] ?: 0.0) }
                val groupAnatomicalWeights = groupWithMuscles.muscles.associate { it.id to it.anatomicalWeight }
                
                val groupScore = aggregateGroupRating(groupMuscleRatings, groupAnatomicalWeights)
                val rank = Rank.fromScore(groupScore.toInt())
                
                Log.d("DashboardVM", "Group: ${groupWithMuscles.group.name}, Score: $groupScore, Rank: $rank")
                
                groupWithMuscles.group.name to rank
            }

            DashboardUiState.Success(
                muscleGroupRanks = groupRanks,
                weeklyWorkoutsCount = workouts.size,
                strengthIncreasePercentage = 0,
                activeEnergyKcal = 0,
                activeTimeHours = 0.0
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
