package com.kidz.workouted.data.repository

import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.data.local.dao.ExerciseWithImpacts
import com.kidz.workouted.data.local.entity.WorkoutEntity
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.domain.model.Workout
import com.kidz.workouted.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun getWorkouts(): Flow<List<Workout>> {
        return dao.getAllWorkouts().map { entities ->
            entities.map { entity ->
                val sets = dao.getSetsForWorkout(entity.id)
                val volume = sets.sumOf { it.weight * it.reps }
                val exercisesCount = sets.distinctBy { it.exerciseId }.size
                entity.toDomain(volume, exercisesCount)
            }
        }
    }

    override suspend fun saveWorkout(timestamp: Long, sets: List<SetEntity>, workoutId: Long) {
        dao.saveWorkoutWithSets(WorkoutEntity(id = workoutId, timestamp = timestamp), sets)
    }

    override suspend fun deleteWorkout(workout: Workout) {
        dao.deleteWorkout(workout.toEntity())
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        return try {
            dao.getWorkoutWithSets(id).workout.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getSetsForWorkout(workoutId: Long): List<SetEntity> {
        return dao.getSetsForWorkout(workoutId)
    }

    override fun getExercisesWithImpacts(): Flow<List<ExerciseWithImpacts>> {
        return dao.getExercisesWithMuscleImpacts()
    }

    private fun WorkoutEntity.toDomain(totalVolume: Double = 0.0, exercisesCount: Int = 0) = Workout(
        id = id,
        timestamp = timestamp,
        totalVolume = totalVolume,
        exercisesCount = exercisesCount
    )

    private fun Workout.toEntity() = WorkoutEntity(
        id = id,
        timestamp = timestamp
    )
}
