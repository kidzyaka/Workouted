package com.kidz.workouted.data.repository

import com.kidz.workouted.data.remote.WorkoutedApi
import com.kidz.workouted.data.remote.model.FriendRequestDto
import com.kidz.workouted.data.remote.model.LeaderboardEntry
import com.kidz.workouted.data.remote.model.OneRepMaxPointDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val api: WorkoutedApi
) {
    suspend fun sendFriendRequest(code: String): Result<Unit> {
        return try {
            api.sendFriendRequest(code)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(friendshipId: Long): Result<Unit> {
        return try {
            api.acceptFriendRequest(friendshipId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendRequests(): Result<List<FriendRequestDto>> {
        return try {
            val requests = api.getFriendRequests()
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> {
        return try {
            val leaderboard = api.getLeaderboard()
            Result.success(leaderboard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends1RmStats(exerciseId: Long): Result<Map<String, List<OneRepMaxPointDto>>> {
        return try {
            val stats = api.getFriends1RmStats(exerciseId)
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendsVolumeStats(): Result<Map<String, List<OneRepMaxPointDto>>> {
        return try {
            val stats = api.getFriendsVolumeStats()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
