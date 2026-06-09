package com.kidz.workouted.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userHeightCm: Flow<Double>
    suspend fun setUserHeightCm(height: Double)
}
