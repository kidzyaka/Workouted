package com.kidz.workouted.domain.usecase

import com.kidz.workouted.data.local.dao.ExerciseWithImpacts
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.data.local.entity.MuscleImpactEntity
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.data.local.entity.WorkoutEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMuscleRatingsUseCaseTest {

    private lateinit var getMuscleRatings: GetMuscleRatingsUseCase
    private val calculateSetEffort = CalculateSetEffortUseCase()
    private val calculateMuscleRating = CalculateMuscleRatingUseCase()

    @Before
    fun setup() {
        getMuscleRatings = GetMuscleRatingsUseCase(calculateSetEffort, calculateMuscleRating)
    }

    @Test
    fun `calculates rating correctly with multiple sets and workouts`() {
        // Exercise 1: Max Weight 100kg. Impact: Muscle A (1.0)
        val exercise1 = ExerciseWithImpacts(
            exercise = ExerciseEntity(id = 1, name = "Ex 1", isWeightBased = true, maxWeightReference = 100.0),
            impacts = listOf(MuscleImpactEntity(1, "m_a", 1.0))
        )
        val exercises = listOf(exercise1)

        // Workout 1 (Today): 2 sets of Ex 1. Set 1: 50kg, 0 reps. Set 2: 60kg, 0 reps.
        // Base Effort for Set 1: (50/100) * 1 * 500 = 250
        // Base Effort for Set 2: (60/100) * 1 * 500 = 300
        // Result for Workout 1: 300 (best set)
        val workout1 = WorkoutEntity(id = 1, timestamp = 1000L)
        val sets1 = listOf(
            SetEntity(workoutId = 1, exerciseId = 1, weight = 50.0, reps = 0),
            SetEntity(workoutId = 1, exerciseId = 1, weight = 60.0, reps = 0)
        )

        // Workout 2 (Yesterday): 1 set of Ex 1: 30kg, 0 reps.
        // Base Effort: (30/100) * 1 * 500 = 150
        val workout2 = WorkoutEntity(id = 2, timestamp = 500L)
        val sets2 = listOf(
            SetEntity(workoutId = 2, exerciseId = 1, weight = 30.0, reps = 0)
        )

        val workouts = listOf(workout1, workout2)
        val sets = sets1 + sets2

        // Calculation:
        // Muscle A in Workout 1: 300
        // Muscle A in Workout 2: 150
        // Muscle A (missing 3rd): 0
        // Moving Average: (300 + 150 + 0) / 3 = 150.0

        val result = getMuscleRatings(workouts, sets, exercises, 175.0)
        
        assertEquals(150.0, result["m_a"] ?: 0.0, 0.01)
    }
    
    @Test
    fun `takes only top 3 scores`() {
        val exercise1 = ExerciseWithImpacts(
            exercise = ExerciseEntity(id = 1, name = "Ex 1", isWeightBased = true, maxWeightReference = 100.0),
            impacts = listOf(MuscleImpactEntity(1, "m_a", 1.0))
        )
        val exercises = listOf(exercise1)

        val workouts = listOf(
            WorkoutEntity(id = 1, timestamp = 1000L),
            WorkoutEntity(id = 2, timestamp = 2000L),
            WorkoutEntity(id = 3, timestamp = 3000L),
            WorkoutEntity(id = 4, timestamp = 4000L) // Newest
        )
        
        // Scores: 100, 200, 300, 400
        // Multiplier is 500. To get score 100: (weight/100) * 500 = 100 => weight = 20
        val sets = listOf(
            SetEntity(workoutId = 1, exerciseId = 1, weight = 20.0, reps = 0), // score 100
            SetEntity(workoutId = 2, exerciseId = 1, weight = 40.0, reps = 0), // score 200
            SetEntity(workoutId = 3, exerciseId = 1, weight = 60.0, reps = 0), // score 300
            SetEntity(workoutId = 4, exerciseId = 1, weight = 80.0, reps = 0)  // score 400
        )

        // Calculation:
        // Top 3 scores (descending timestamp): 400, 300, 200
        // Average: (400 + 300 + 200) / 3 = 300.0

        val result = getMuscleRatings(workouts, sets, exercises, 175.0)
        
        assertEquals(300.0, result["m_a"] ?: 0.0, 0.01)
    }
}
