package com.kidz.workouted.data.repository

import com.kidz.workouted.data.remote.WorkoutedApi
import com.kidz.workouted.domain.model.BackupData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val api: WorkoutedApi
) {
    suspend fun pushBackup(backupData: BackupData): Result<Unit> {
        return try {
            api.pushBackup(backupData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullBackup(): Result<BackupData> {
        return try {
            val backup = api.pullBackup()
            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
