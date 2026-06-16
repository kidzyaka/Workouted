package com.kidz.workouted.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userHeightCm: Flow<Double>
    val userWeightKg: Flow<Double>
    val userAge: Flow<Int>
    val appLanguage: Flow<String>
    val isOnboardingCompleted: Flow<Boolean>
    val lastSeenMuscleRanks: Flow<Map<String, String>>
    
    suspend fun setUserHeightCm(height: Double)
    suspend fun setUserWeightKg(weight: Double)
    suspend fun setUserAge(age: Int)
    suspend fun setAppLanguage(languageCode: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun updateLastSeenMuscleRanks(ranks: Map<String, String>)
}
