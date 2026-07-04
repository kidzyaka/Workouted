package com.kidz.workouted.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val notes: String? = null
)

@Serializable
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isWeightBased: Boolean,
    val maxWeightReference: Double
)

@Serializable
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val weight: Double,
    val reps: Int
)

@Serializable
@Entity(tableName = "muscle_groups")
data class MuscleGroupEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Serializable
@Entity(
    tableName = "muscles",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MuscleEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
    val anatomicalWeight: Double
)

@Serializable
@Entity(
    tableName = "muscle_impacts",
    primaryKeys = ["exerciseId", "muscleId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MuscleEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MuscleImpactEntity(
    val exerciseId: Long,
    val muscleId: String,
    val impactCoefficient: Double
)
