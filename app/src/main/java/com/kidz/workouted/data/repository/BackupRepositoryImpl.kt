package com.kidz.workouted.data.repository

import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.data.local.entity.SetEntity
import com.kidz.workouted.data.local.entity.WorkoutEntity
import com.kidz.workouted.domain.model.BackupData
import com.kidz.workouted.domain.model.UserPreferencesBackup
import com.kidz.workouted.domain.repository.BackupRepository
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val preferencesRepository: UserPreferencesRepository
) : BackupRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true 
    }

    override suspend fun getBackupData(): BackupData {
        val workouts = workoutDao.getAllWorkouts().first()
        val sets = workoutDao.getAllSets().first()
        
        val height = preferencesRepository.userHeightCm.first()
        val weight = preferencesRepository.userWeightKg.first()
        val age = preferencesRepository.userAge.first()
        val language = preferencesRepository.appLanguage.first()
        val onboarding = preferencesRepository.isOnboardingCompleted.first()
        val ranks = preferencesRepository.lastSeenMuscleRanks.first()
        val color = preferencesRepository.userColor.first()
        val theme = preferencesRepository.appTheme.first()

        return BackupData(
            workouts = workouts,
            sets = sets,
            preferences = UserPreferencesBackup(
                height = height,
                weight = weight,
                age = age,
                language = language,
                isOnboardingCompleted = onboarding,
                defaultColor = color,
                lastSeenMuscleRanks = ranks,
                appTheme = theme
            )
        )
    }

    override suspend fun exportData(): String {
        return json.encodeToString(getBackupData())
    }

    override suspend fun restoreFromBackupData(data: BackupData): Result<Unit> {
        return try {
            // Restore Preferences
            preferencesRepository.setUserHeightCm(data.preferences.height)
            preferencesRepository.setUserWeightKg(data.preferences.weight)
            preferencesRepository.setUserAge(data.preferences.age)
            preferencesRepository.setAppLanguage(data.preferences.language)
            preferencesRepository.setOnboardingCompleted(data.preferences.isOnboardingCompleted)
            preferencesRepository.updateLastSeenMuscleRanks(data.preferences.lastSeenMuscleRanks)
            if (data.preferences.defaultColor != null) {
                preferencesRepository.setUserColor(data.preferences.defaultColor)
            }
            if (data.preferences.appTheme != null) {
                preferencesRepository.setAppTheme(data.preferences.appTheme)
            }

            // Restore Database
            workoutDao.importWorkoutsAndSets(data.workouts, data.sets)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importData(jsonString: String): Result<Unit> {
        return try {
            val data = json.decodeFromString<BackupData>(jsonString)
            restoreFromBackupData(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
