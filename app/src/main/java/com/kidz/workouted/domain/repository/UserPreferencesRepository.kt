package com.kidz.workouted.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userHeightCm: Flow<Double>
    val userWeightKg: Flow<Double>
    val userAge: Flow<Int>
    
    suspend fun setUserHeightCm(height: Double)
    suspend fun setUserWeightKg(weight: Double)
    suspend fun setUserAge(age: Int)
}
