package com.kidz.workouted.domain.repository

import com.kidz.workouted.domain.model.Workout
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.data.local.dao.ExerciseWithImpacts
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getWorkouts(): Flow<List<Workout>>
    suspend fun saveWorkout(timestamp: Long, sets: List<SetEntity>, workoutId: Long = 0)
    suspend fun deleteWorkout(workout: Workout)
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun getSetsForWorkout(workoutId: Long): List<SetEntity>
    fun getExercisesWithImpacts(): Flow<List<ExerciseWithImpacts>>
}
