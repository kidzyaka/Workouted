package com.kidz.workouted.domain.repository

import com.kidz.workouted.domain.model.BackupData

interface BackupRepository {
    suspend fun exportData(): String
    suspend fun importData(jsonString: String): Result<Unit>
    suspend fun getBackupData(): BackupData
    suspend fun restoreFromBackupData(data: BackupData): Result<Unit>
}
