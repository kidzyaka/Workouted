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
            
            val exerciseMap = exercises.associateBy { it.exercise.id }
            val workoutMap = workouts.associateBy { it.id }
            
            // Pre-calculate effort for each set to use in multiple charts
            val setEfforts = sets.map { set ->
                val ex = exerciseMap[set.exerciseId]
                val effort = if (ex != null) {
                    calculateSetEffort(set.weight, set.reps, ex.exercise.maxWeightReference, height)
                } else 0.0
                set to effort
            }

            // 1. Total Daily Load (Effort) for last 7 days
            val dailyEfforts = setEfforts.groupBy { (set, _) ->
                val workout = workoutMap[set.workoutId]
                if (workout == null) 0L
                else {
                    Calendar.getInstance().apply { 
                        timeInMillis = workout.timestamp
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            }.mapValues { (_, efforts) -> efforts.sumOf { it.second } }

            val maxDailyEffort = dailyEfforts.values.maxOrNull()?.coerceAtLeast(500.0) ?: 500.0

            val activityData = (0..6).reversed().map { daysAgo ->
                val dayCalendar = Calendar.getInstance()
                dayCalendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dayStart = dayCalendar.apply { 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val dayEffort = dailyEfforts[dayStart] ?: 0.0
                val dayLabel = SimpleDateFormat("E", Locale.getDefault()).format(dayCalendar.time).first().toString()
                ActivityData(dayLabel, (dayEffort / maxDailyEffort).toFloat().coerceAtLeast(0.05f))
            }

            // 2. Muscle Balance Calculation
            val muscleRatings = mutableMapOf<Long, Double>()
            
            setEfforts.forEach { (set, baseEffort) ->
                val ex = exerciseMap[set.exerciseId]
                ex?.impacts?.forEach { impact ->
                    val points = calculateMuscleRating(baseEffort, impact.impactCoefficient)
                    muscleRatings[impact.muscleId] = (muscleRatings[impact.muscleId] ?: 0.0) + points
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
