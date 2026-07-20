package com.kidz.workouted.domain.model

import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.data.local.entity.WorkoutEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val workouts: List<WorkoutEntity>,
    val sets: List<SetEntity>,
    val preferences: UserPreferencesBackup,
    val force: Boolean = false
)

@Serializable
data class UserPreferencesBackup(
    val height: Double,
    val weight: Double,
    val age: Int,
    val language: String,
    val isOnboardingCompleted: Boolean,
    val defaultColor: String? = null,
    val lastSeenMuscleRanks: Map<String, String>,
    val appTheme: String? = null
)
