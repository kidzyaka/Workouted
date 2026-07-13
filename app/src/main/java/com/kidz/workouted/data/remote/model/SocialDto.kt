package com.kidz.workouted.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FriendRequestDto(
    val friendshipId: Long,
    val requesterUsername: String,
    val requesterCode: String
)

@Serializable
data class MuscleScoreDto(
    val score: Double,
    val rank: String
)

@Serializable
data class LeaderboardEntry(
    val friendId: Long,
    val username: String,
    val friendCode: String,
    val height: Double? = null,
    val weight: Double? = null,
    val age: Int? = null,
    val defaultColor: String? = null,
    val totalScore: Double,
    val muscleScores: Map<String, MuscleScoreDto>,
    val recentWorkoutTimestamps: List<Long>
)

@Serializable
data class OneRepMaxPointDto(
    val timestamp: Long,
    val oneRm: Double
)
