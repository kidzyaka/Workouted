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
        // Base Effort for Set 1: (50/100) * 1 * 400 = 200
        // Base Effort for Set 2: (60/100) * 1 * 400 = 240
        // Result for Workout 1: 240 (best set)
        val workout1 = WorkoutEntity(id = 1, timestamp = 1000L)
        val sets1 = listOf(
            SetEntity(workoutId = 1, exerciseId = 1, weight = 50.0, reps = 0),
            SetEntity(workoutId = 1, exerciseId = 1, weight = 60.0, reps = 0)
        )

        // Workout 2 (Yesterday): 1 set of Ex 1: 30kg, 0 reps.
        // Base Effort: (30/100) * 1 * 400 = 120
        val workout2 = WorkoutEntity(id = 2, timestamp = 500L)
        val sets2 = listOf(
            SetEntity(workoutId = 2, exerciseId = 1, weight = 30.0, reps = 0)
        )

        val workouts = listOf(workout1, workout2)
        val sets = sets1 + sets2

        // Calculation:
        // Muscle A in Workout 1: 240
        // Muscle A in Workout 2: 120
        // Logic: max of last 3 active workouts
        // Result: 240.0

        val result = getMuscleRatings(workouts, sets, exercises, 175.0)
        
        assertEquals(240.0, result["m_a"] ?: 0.0, 0.01)
    }
    
    @Test
    fun `takes only top 3 scores and picks the max`() {
        val exercise1 = ExerciseWithImpacts(
            exercise = ExerciseEntity(id = 1, name = "Ex 1", isWeightBased = true, maxWeightReference = 100.0),
            impacts = listOf(MuscleImpactEntity(1, "m_a", 1.0))
        )
        val exercises = listOf(exercise1)

        val workouts = listOf(
            WorkoutEntity(id = 1, timestamp = 1000L),
            WorkoutEntity(id = 2, timestamp = 2000L),
            WorkoutEntity(id = 3, timestamp = 3000L),
            WorkoutEntity(id = 4, timestamp = 4000L) // Latest
        )
        
        // Multiplier is 400.
        // weight 20 => (20/100) * 400 = 80
        // weight 40 => (40/100) * 400 = 160
        // weight 60 => (60/100) * 400 = 240
        // weight 80 => (80/100) * 400 = 320
        val sets = listOf(
            SetEntity(workoutId = 1, exerciseId = 1, weight = 20.0, reps = 0), // score 80 (too old)
            SetEntity(workoutId = 2, exerciseId = 1, weight = 80.0, reps = 0), // score 320
            SetEntity(workoutId = 3, exerciseId = 1, weight = 40.0, reps = 0), // score 160
            SetEntity(workoutId = 4, exerciseId = 1, weight = 60.0, reps = 0)  // score 240 (latest)
        )

        // Calculation:
        // Last 3 active workouts (descending timestamp): W4 (240), W3 (160), W2 (320)
        // Logic: max(240, 160, 320) = 320.0

        val result = getMuscleRatings(workouts, sets, exercises, 175.0)
        
        assertEquals(320.0, result["m_a"] ?: 0.0, 0.01)
    }
}
