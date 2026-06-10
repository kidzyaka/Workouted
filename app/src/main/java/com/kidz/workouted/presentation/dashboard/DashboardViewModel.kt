package com.kidz.workouted.presentation.dashboard

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
            
            val groupRanks = groups.associate { groupWithMuscles ->
                val groupMuscleRatings = groupWithMuscles.muscles.associate { it.id to (muscleRatings[it.id] ?: 0.0) }
                val groupAnatomicalWeights = groupWithMuscles.muscles.associate { it.id to it.anatomicalWeight }
                
                val groupScore = aggregateGroupRating(groupMuscleRatings, groupAnatomicalWeights)
                groupWithMuscles.group.name to Rank.fromScore(groupScore.toInt())
            }

            // Calculate weekly load
            val last7Days = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val weeklyWorkouts = workouts.filter { it.timestamp >= last7Days }
            
            DashboardUiState.Success(
                muscleGroupRanks = groupRanks,
                weeklyWorkoutsCount = weeklyWorkouts.size,
                strengthIncreasePercentage = 0,
                activeEnergyKcal = 0,
                activeTimeHours = 0.0
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
