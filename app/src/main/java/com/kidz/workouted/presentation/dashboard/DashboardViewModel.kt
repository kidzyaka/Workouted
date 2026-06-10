package com.kidz.workouted.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import com.kidz.workouted.domain.usecase.AggregateGroupRatingUseCase
import com.kidz.workouted.domain.usecase.GetMuscleRatingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val getMuscleRatings: GetMuscleRatingsUseCase,
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
            
            Log.d("DashboardVM", "Loaded ${exercises.size} exercises from DB.")
            
            val muscleRatings = getMuscleRatings(workouts, sets, exercises, height)
            
            val groupRanks = groups.associate { groupWithMuscles ->
                val groupMuscleRatings = groupWithMuscles.muscles.associate { it.id to (muscleRatings[it.id] ?: 0.0) }
                val groupAnatomicalWeights = groupWithMuscles.muscles.associate { it.id to it.anatomicalWeight }
                
                val groupScore = aggregateGroupRating(groupMuscleRatings, groupAnatomicalWeights)
                groupWithMuscles.group.id to Rank.fromScore(groupScore.toInt())
            }

            // Calculate weekly load
            val last7Days = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val weeklyWorkouts = workouts.filter { it.timestamp >= last7Days }
            
            DashboardUiState.Success(
                muscleGroupRanks = groupRanks,
                workoutDates = workouts.map { it.timestamp }.toSet(),
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
