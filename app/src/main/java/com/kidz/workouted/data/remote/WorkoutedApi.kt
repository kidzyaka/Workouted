package com.kidz.workouted.data.remote

import com.kidz.workouted.data.remote.model.*
import com.kidz.workouted.domain.model.BackupData
import retrofit2.http.*

interface WorkoutedApi {
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @DELETE("auth/account")
    suspend fun deleteAccount()

    @POST("sync/backup")
    suspend fun pushBackup(@Body backupData: BackupData)

    @GET("sync/backup")
    suspend fun pullBackup(): BackupData

    @POST("friends/request")
    suspend fun sendFriendRequest(@Query("code") code: String)

    @POST("friends/accept")
    suspend fun acceptFriendRequest(@Query("friendshipId") friendshipId: Long)

    @POST("friends/reject")
    suspend fun rejectFriendRequest(@Query("friendshipId") friendshipId: Long)

    @POST("friends/remove")
    suspend fun removeFriend(@Query("friendId") friendId: Long)

    @GET("friends/requests")
    suspend fun getFriendRequests(): List<FriendRequestDto>

    @GET("friends/leaderboard")
    suspend fun getLeaderboard(): List<LeaderboardEntry>

    @GET("friends/stats/1rm")
    suspend fun getFriends1RmStats(@Query("exerciseId") exerciseId: Long): Map<String, List<OneRepMaxPointDto>>

    @GET("friends/stats/volume")
    suspend fun getFriendsVolumeStats(): Map<String, List<OneRepMaxPointDto>>
}
