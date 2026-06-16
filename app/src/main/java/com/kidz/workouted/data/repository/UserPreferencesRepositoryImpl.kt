package com.kidz.workouted.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val USER_HEIGHT = doublePreferencesKey("user_height")
        val USER_WEIGHT = doublePreferencesKey("user_weight")
        val USER_AGE = intPreferencesKey("user_age")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val LAST_SEEN_MUSCLE_RANKS = stringPreferencesKey("last_seen_muscle_ranks")
    }

    override val userHeightCm: Flow<Double> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.USER_HEIGHT] ?: 175.0 }

    override val userWeightKg: Flow<Double> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.USER_WEIGHT] ?: 75.0 }

    override val userAge: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.USER_AGE] ?: 25 }

    override val appLanguage: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.APP_LANGUAGE] ?: Locale.getDefault().language }

    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false }

    override val lastSeenMuscleRanks: Flow<Map<String, String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val serialized = prefs[PreferencesKeys.LAST_SEEN_MUSCLE_RANKS] ?: ""
            if (serialized.isEmpty()) emptyMap()
            else serialized.split(",").associate {
                val parts = it.split(":")
                if (parts.size == 2) parts[0] to parts[1] else "" to ""
            }.filterKeys { it.isNotEmpty() }
        }

    override suspend fun setUserHeightCm(height: Double) {
        context.dataStore.edit { it[PreferencesKeys.USER_HEIGHT] = height }
    }

    override suspend fun setUserWeightKg(weight: Double) {
        context.dataStore.edit { it[PreferencesKeys.USER_WEIGHT] = weight }
    }

    override suspend fun setUserAge(age: Int) {
        context.dataStore.edit { it[PreferencesKeys.USER_AGE] = age }
    }

    override suspend fun setAppLanguage(languageCode: String) {
        context.dataStore.edit { it[PreferencesKeys.APP_LANGUAGE] = languageCode }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun updateLastSeenMuscleRanks(ranks: Map<String, String>) {
        val serialized = ranks.entries.joinToString(",") { "${it.key}:${it.value}" }
        context.dataStore.edit { it[PreferencesKeys.LAST_SEEN_MUSCLE_RANKS] = serialized }
    }
}
