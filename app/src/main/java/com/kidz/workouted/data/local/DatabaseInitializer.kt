package com.kidz.workouted.data.local

import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.data.local.entity.MuscleEntity
import com.kidz.workouted.data.local.entity.MuscleGroupEntity
import com.kidz.workouted.data.local.entity.MuscleImpactEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val db: WorkoutedDatabase
) {
    suspend fun seedIfNeeded() {
        val exerciseCount = db.workoutDao.getAllExercises().first().size
        if (exerciseCount > 0) return

        // Exercises
        val benchPressId = db.workoutDao.insertExercise(
            ExerciseEntity(name = "Bench Press", isWeightBased = true, maxWeightReference = 150.0)
        )
        val pullUpId = db.workoutDao.insertExercise(
            ExerciseEntity(name = "Pull Up", isWeightBased = true, maxWeightReference = 100.0)
        )
        val squatId = db.workoutDao.insertExercise(
            ExerciseEntity(name = "Squat", isWeightBased = true, maxWeightReference = 200.0)
        )

        // Impacts (using new string IDs from callback)
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(benchPressId, "m_chest_mid", 1.0))
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(benchPressId, "m_triceps", 0.4))
        
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(pullUpId, "m_lats", 1.0))
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(pullUpId, "m_biceps", 0.5))
        
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(squatId, "m_quads", 1.0))
    }
}
