package com.kidz.workouted.data.repository

import com.kidz.workouted.data.remote.WorkoutedApi
import com.kidz.workouted.data.remote.model.AuthRequest
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: WorkoutedApi,
    private val preferences: UserPreferencesRepository
) {
    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = api.login(AuthRequest(username, password))
            preferences.setJwtToken(response.token)
            preferences.setFriendCode(response.friendCode)
            Result.success(Unit)
        } catch (e: retrofit2.HttpException) {
            val errorMessage = extractErrorMessage(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, password: String): Result<Unit> {
        return try {
            val response = api.register(AuthRequest(username, password))
            preferences.setJwtToken(response.token)
            preferences.setFriendCode(response.friendCode)
            Result.success(Unit)
        } catch (e: retrofit2.HttpException) {
            val errorMessage = extractErrorMessage(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(e: retrofit2.HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                org.json.JSONObject(errorBody).getString("error")
            } else {
                e.message() ?: "HTTP Error ${e.code()}"
            }
        } catch (ex: Exception) {
            e.message() ?: "HTTP Error ${e.code()}"
        }
    }

    suspend fun logout() {
        preferences.setJwtToken(null)
        preferences.setFriendCode(null)
    }
}
