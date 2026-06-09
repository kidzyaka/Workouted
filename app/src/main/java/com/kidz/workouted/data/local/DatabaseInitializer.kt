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

        // Groups
        val chestGroupId = db.workoutDao.insertMuscleGroup(MuscleGroupEntity(name = "CHEST"))
        val backGroupId = db.workoutDao.insertMuscleGroup(MuscleGroupEntity(name = "BACK"))
        val legsGroupId = db.workoutDao.insertMuscleGroup(MuscleGroupEntity(name = "LEGS"))
        val armsGroupId = db.workoutDao.insertMuscleGroup(MuscleGroupEntity(name = "ARMS"))

        // Muscles
        val pecId = db.workoutDao.insertMuscle(MuscleEntity(groupId = chestGroupId, name = "Pectoralis", anatomicalWeight = 1.0))
        val latId = db.workoutDao.insertMuscle(MuscleEntity(groupId = backGroupId, name = "Lats", anatomicalWeight = 1.2))
        val quadId = db.workoutDao.insertMuscle(MuscleEntity(groupId = legsGroupId, name = "Quads", anatomicalWeight = 1.5))
        val bicepId = db.workoutDao.insertMuscle(MuscleEntity(groupId = armsGroupId, name = "Bicep", anatomicalWeight = 0.5))
        val tricepId = db.workoutDao.insertMuscle(MuscleEntity(groupId = armsGroupId, name = "Tricep", anatomicalWeight = 0.6))

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

        // Impacts
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(benchPressId, pecId, 1.0))
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(benchPressId, tricepId, 0.4))
        
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(pullUpId, latId, 1.0))
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(pullUpId, bicepId, 0.5))
        
        db.workoutDao.insertMuscleImpact(MuscleImpactEntity(squatId, quadId, 1.0))
    }
}
