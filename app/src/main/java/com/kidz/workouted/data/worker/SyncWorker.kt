package com.kidz.workouted.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kidz.workouted.data.repository.SyncRepository
import com.kidz.workouted.domain.repository.BackupRepository
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val backupRepository: BackupRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val token = userPreferencesRepository.jwtToken.first()
            if (token.isNullOrEmpty()) {
                return Result.success()
            }

            val hasChanges = userPreferencesRepository.hasUnsyncedChanges.first()
            if (!hasChanges) {
                return Result.success()
            }

            val data = backupRepository.getBackupData(force = false)
            val result = syncRepository.pushBackup(data)

            if (result.isSuccess) {
                userPreferencesRepository.setHasUnsyncedChanges(false)
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                if (error is retrofit2.HttpException && error.code() == 409) {
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
