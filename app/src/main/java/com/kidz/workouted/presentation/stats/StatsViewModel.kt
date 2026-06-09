package com.kidz.workouted.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import com.kidz.workouted.domain.usecase.AggregateGroupRatingUseCase
import com.kidz.workouted.domain.usecase.CalculateMuscleRatingUseCase
import com.kidz.workouted.domain.usecase.CalculateOneRepMaxUseCase
import com.kidz.workouted.domain.usecase.CalculateSetEffortUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val calculateSetEffort: CalculateSetEffortUseCase,
    private val calculateMuscleRating: CalculateMuscleRatingUseCase,
    private val aggregateGroupRating: AggregateGroupRatingUseCase,
    private val calculateOneRepMax: CalculateOneRepMaxUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        combine(
            workoutDao.getAllWorkouts(),
            workoutDao.getAllSets(),
            workoutDao.getExercisesWithMuscleImpacts(),
            workoutDao.getMuscleGroupsWithMuscles(),
            preferencesRepository.userHeightCm
        ) { workouts, sets, exercises, groups, height ->
            
            // 1. Activity Calculation (Last 7 Days)
            val calendar = Calendar.getInstance()
            val activityData = (0..6).reversed().map { daysAgo ->
                val dayCalendar = Calendar.getInstance()
                dayCalendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dayStart = dayCalendar.apply { 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                val dayEnd = dayCalendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                
                val workoutsCount = workouts.count { it.timestamp in dayStart..dayEnd }
                val dayLabel = SimpleDateFormat("E", Locale.getDefault()).format(dayCalendar.time).first().toString()
                ActivityData(dayLabel, if (workoutsCount > 0) 0.3f + (workoutsCount * 0.2f).coerceAtMost(0.7f) else 0.05f)
            }

            // 2. Muscle Balance Calculation
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

            val groupBalance = groups.associate { groupWithMuscles ->
                val groupMuscleRatings = groupWithMuscles.muscles.associate { it.id to (muscleRatings[it.id] ?: 0.0) }
                val groupAnatomicalWeights = groupWithMuscles.muscles.associate { it.id to it.anatomicalWeight }
                val groupScore = aggregateGroupRating(groupMuscleRatings, groupAnatomicalWeights)
                groupWithMuscles.group.name to groupScore.toFloat()
            }

            // Normalize balance for Radar Chart (max score as reference)
            val maxScore = groupBalance.values.maxOrNull()?.coerceAtLeast(100f) ?: 100f
            val normalizedBalance = groupBalance.mapValues { it.value / maxScore }

            // 3. 1RM Progress (for the most frequent exercise)
            val progressData = if (sets.isNotEmpty()) {
                val mostFrequentExerciseId = sets.groupBy { it.exerciseId }
                    .maxByOrNull { it.value.size }?.key
                
                val exerciseSets = sets.filter { it.exerciseId == mostFrequentExerciseId }
                val workoutMap = workouts.associateBy { it.id }
                
                exerciseSets.groupBy { it.workoutId }
                    .mapNotNull { (workoutId, sets) ->
                        val workout = workoutMap[workoutId] ?: return@mapNotNull null
                        val max1RM = sets.maxOf { calculateOneRepMax(it.weight, it.reps) }
                        workout.timestamp to max1RM
                    }
                    .sortedBy { it.first }
                    .takeLast(6)
                    .map { (timestamp, oneRM) ->
                        ProgressData(
                            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp)),
                            oneRM.toFloat()
                        )
                    }
            } else emptyList()

            StatsUiState.Success(
                activityData = activityData,
                progressData = progressData,
                muscleBalance = normalizedBalance
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
