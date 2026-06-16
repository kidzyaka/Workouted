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
import kotlinx.coroutines.launch
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
            preferencesRepository.userHeightCm,
            preferencesRepository.lastSeenMuscleRanks
        ) { args ->
            val workouts = args[0] as List<com.kidz.workouted.data.local.entity.WorkoutEntity>
            val sets = args[1] as List<com.kidz.workouted.data.local.entity.SetEntity>
            val exercises = args[2] as List<com.kidz.workouted.data.local.dao.ExerciseWithImpacts>
            val groups = args[3] as List<com.kidz.workouted.data.local.dao.MuscleGroupWithMuscles>
            val height = args[4] as Double
            val lastSeenRanks = args[5] as Map<String, String>
            
            Log.d("DashboardVM", "Loaded ${exercises.size} exercises from DB.")
            
            val muscleRatings = getMuscleRatings(workouts, sets, exercises, height)
            
            val muscleGroupsProgression = groups.associate { groupWithMuscles ->
                val groupMuscleRatings = groupWithMuscles.muscles.associate { it.id to (muscleRatings[it.id] ?: 0.0) }
                val groupAnatomicalWeights = groupWithMuscles.muscles.associate { it.id to it.anatomicalWeight }
                
                val groupScore = aggregateGroupRating(groupMuscleRatings, groupAnatomicalWeights)
                val groupId = groupWithMuscles.group.id
                
                var groupHasUnseen = false
                val musclesProgression = groupWithMuscles.muscles.map { muscle ->
                    val score = muscleRatings[muscle.id] ?: 0.0
                    val currentRank = Rank.fromScore(score.toInt())
                    val lastSeenRankName = lastSeenRanks[muscle.id]
                    
                    val isIncreased = if (lastSeenRankName != null) {
                        try {
                            val lastSeenRank = Rank.valueOf(lastSeenRankName)
                            currentRank.minScore > lastSeenRank.minScore
                        } catch (_: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                    
                    if (isIncreased) groupHasUnseen = true
                    
                    MuscleProgression(
                        id = muscle.id,
                        score = score,
                        rank = currentRank,
                        isRankIncreased = isIncreased
                    )
                }
                
                groupId to MuscleGroupProgression(
                    id = groupId,
                    score = groupScore,
                    rank = Rank.fromScore(groupScore.toInt()),
                    muscles = musclesProgression,
                    hasUnseenProgression = groupHasUnseen
                )
            }

            val groupRanks = muscleGroupsProgression.mapValues { it.value.rank }

            // Calculate weekly load
            val last7Days = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val weeklyWorkouts = workouts.filter { it.timestamp >= last7Days }
            
            DashboardUiState.Success(
                muscleGroupRanks = groupRanks,
                muscleGroupsProgression = muscleGroupsProgression,
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

    fun onMuscleGroupSeen(progression: MuscleGroupProgression) {
        viewModelScope.launch {
            val currentLastSeen = preferencesRepository.lastSeenMuscleRanks.first().toMutableMap()
            var changed = false
            progression.muscles.forEach { muscle ->
                val existing = currentLastSeen[muscle.id]
                if (existing != muscle.rank.name) {
                    currentLastSeen[muscle.id] = muscle.rank.name
                    changed = true
                }
            }
            if (changed) {
                preferencesRepository.updateLastSeenMuscleRanks(currentLastSeen)
            }
        }
    }
}
