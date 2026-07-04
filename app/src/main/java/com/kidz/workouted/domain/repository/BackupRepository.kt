package com.kidz.workouted.domain.repository

interface BackupRepository {
    suspend fun exportData(): String
    suspend fun importData(jsonString: String): Result<Unit>
}
