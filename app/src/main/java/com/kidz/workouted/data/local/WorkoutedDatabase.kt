package com.kidz.workouted.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.data.local.entity.*

@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        MuscleEntity::class,
        MuscleGroupEntity::class,
        MuscleImpactEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class WorkoutedDatabase : RoomDatabase() {
    abstract val workoutDao: WorkoutDao

    companion object {
        const val DATABASE_NAME = "workouted_db"
    }
}
