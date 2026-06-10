package com.kidz.workouted.data.local.dao

import androidx.room.*
import com.kidz.workouted.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetEntity>)

    @Query("SELECT * FROM sets WHERE workoutId = :workoutId")
    suspend fun getSetsForWorkout(workoutId: Long): List<SetEntity>

    @Query("DELETE FROM sets WHERE workoutId = :workoutId")
    suspend fun deleteSetsForWorkout(workoutId: Long)

    @Transaction
    suspend fun saveWorkoutWithSets(workout: WorkoutEntity, sets: List<SetEntity>) {
        val workoutId = if (workout.id == 0L) {
            insertWorkout(workout)
        } else {
            // Update existing workout
            insertWorkout(workout) // Replaces if exists
            deleteSetsForWorkout(workout.id)
            workout.id
        }
        val setsWithId = sets.map { it.copy(workoutId = workoutId) }
        insertSets(setsWithId)
    }

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithSets(workoutId: Long): WorkoutWithSets

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscleGroup(group: MuscleGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscle(muscle: MuscleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscleImpact(impact: MuscleImpactEntity)

    @Query("SELECT * FROM muscle_groups")
    fun getAllMuscleGroups(): Flow<List<MuscleGroupEntity>>

    @Query("SELECT * FROM muscles")
    fun getAllMuscles(): Flow<List<MuscleEntity>>

    @Transaction
    @Query("SELECT * FROM muscle_groups")
    fun getMuscleGroupsWithMuscles(): Flow<List<MuscleGroupWithMuscles>>

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM sets")
    fun getAllSets(): Flow<List<SetEntity>>

    @Transaction
    @Query("SELECT * FROM exercises")
    fun getExercisesWithMuscleImpacts(): Flow<List<ExerciseWithImpacts>>
}

data class WorkoutWithSets(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val sets: List<SetEntity>
)

data class ExerciseWithImpacts(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val impacts: List<MuscleImpactEntity>
)

data class MuscleGroupWithMuscles(
    @Embedded val group: MuscleGroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val muscles: List<MuscleEntity>
)
