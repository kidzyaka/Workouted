package com.kidz.workouted.domain.usecase

import com.kidz.workouted.data.local.dao.ExerciseWithImpacts
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.data.local.entity.WorkoutEntity
import javax.inject.Inject

class GetMuscleRatingsUseCase @Inject constructor(
    private val calculateSetEffort: CalculateSetEffortUseCase,
    private val calculateMuscleRating: CalculateMuscleRatingUseCase
) {
    operator fun invoke(
        workouts: List<WorkoutEntity>,
        sets: List<SetEntity>,
        exercises: List<ExerciseWithImpacts>,
        userHeightCm: Double
    ): Map<String, Double> {
        val exerciseMap = exercises.associateBy { it.exercise.id }
        val workoutMap = workouts.associateBy { it.id }
        
        // 1. Calculate best effort for each exercise in each workout
        val workoutExerciseEfforts = sets.groupBy { it.workoutId }.mapValues { (_, workoutSets) ->
            workoutSets.groupBy { it.exerciseId }.mapValues { (_, exerciseSets) ->
                exerciseSets.maxOf { set ->
                    val exWithImpacts = exerciseMap[set.exerciseId]
                    if (exWithImpacts != null) {
                        calculateSetEffort(set.weight, set.reps, exWithImpacts.exercise.maxWeightReference, userHeightCm)
                    } else 0.0
                }
            }
        }

        // 2. Calculate muscle points for each workout
        val workoutMuscleScores = workoutExerciseEfforts.mapValues { (_, efforts) ->
            val muscleScores = mutableMapOf<String, Double>()
            efforts.forEach { (exerciseId, maxEffort) ->
                val exWithImpacts = exerciseMap[exerciseId]
                exWithImpacts?.impacts?.forEach { impact ->
                    val points = calculateMuscleRating(maxEffort, impact.impactCoefficient)
                    muscleScores[impact.muscleId] = (muscleScores[impact.muscleId] ?: 0.0) + points
                }
            }
            muscleScores
        }

        // 3. Identify all unique muscle IDs
        val allMuscleIds = exercises.flatMap { it.impacts.map { imp -> imp.muscleId } }.distinct()

        // 4. Calculate moving average (last 3 workouts) for each muscle
        return allMuscleIds.associateWith { muscleId ->
            val scoresForMuscle = workoutMuscleScores.mapNotNull { (workoutId, scores) ->
                val score = scores[muscleId] ?: 0.0
                if (score > 0) {
                    val timestamp = workoutMap[workoutId]?.timestamp ?: 0L
                    timestamp to score
                } else null
            }
            
            val activeScores = scoresForMuscle
                .sortedByDescending { it.first }
                .take(3)
                .map { it.second }
            
            if (activeScores.isEmpty()) 0.0
            else activeScores.maxOrNull() ?: 0.0 // Rank reflects current peak capability
        }
    }
}
