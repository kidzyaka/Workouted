package com.kidz.workouted.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import com.kidz.workouted.domain.usecase.*
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
    private val calculateOneRepMax: CalculateOneRepMaxUseCase,
    private val getMuscleRatings: GetMuscleRatingsUseCase,
    private val aggregateGroupRating: AggregateGroupRatingUseCase
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
            
            // 1. Calculate workout efforts using the "best set per exercise" rule
            val workoutExerciseEfforts = sets.groupBy { it.workoutId }.mapValues { (_, workoutSets) ->
                workoutSets.groupBy { it.exerciseId }.mapValues { (_, exerciseSets) ->
                    exerciseSets.maxOf { set ->
                        val exWithImpacts = exerciseMap[set.exerciseId]
                        if (exWithImpacts != null) {
                            calculateSetEffort(set.weight, set.reps, exWithImpacts.exercise.maxWeightReference, height)
                        } else 0.0
                    }
                }
            }
            
            val workoutTotalEfforts = workoutExerciseEfforts.mapValues { (_, efforts) -> efforts.values.sum() }

            // 2. Total Daily Load (Effort) for last 7 days
            val dailyEfforts = workoutTotalEfforts.entries.groupBy { (workoutId, _) ->
                val workout = workoutMap[workoutId]
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
            }.mapValues { (_, entries) -> entries.sumOf { it.value } }

            val maxDailyEffort = dailyEfforts.values.maxOrNull()?.coerceAtLeast(300.0) ?: 300.0

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

            // 3. Muscle Balance Calculation (using the refined GetMuscleRatingsUseCase)
            val muscleRatings = getMuscleRatings(workouts, sets, exercises, height)

            val groupScores = groups.associate { groupWithMuscles ->
                val groupMuscleRatings = groupWithMuscles.muscles.associate { it.id to (muscleRatings[it.id] ?: 0.0) }
                val groupAnatomicalWeights = groupWithMuscles.muscles.associate { it.id to it.anatomicalWeight }
                val groupScore = aggregateGroupRating(groupMuscleRatings, groupAnatomicalWeights)
                // Use ID as key for localized resolution in UI
                groupWithMuscles.group.id to groupScore.toFloat()
            }

            val muscleProgression = groupScores.map { (id, score) ->
                MuscleProgression(id, score) // id here is "group_chest" etc
            }

            // Normalize balance for Radar Chart (max score as reference)
            val maxScore = groupScores.values.maxOrNull()?.coerceAtLeast(100f) ?: 100f
            val normalizedBalance = groupScores.mapValues { it.value / maxScore }

            // 4. 1RM Progress (for the most frequent exercise)
            val progressData = if (sets.isNotEmpty()) {
                val mostFrequentExerciseId = sets.groupBy { it.exerciseId }
                    .maxByOrNull { it.value.size }?.key
                
                val exerciseSets = sets.filter { it.exerciseId == mostFrequentExerciseId }
                
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
                muscleBalance = normalizedBalance,
                muscleProgression = muscleProgression
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
