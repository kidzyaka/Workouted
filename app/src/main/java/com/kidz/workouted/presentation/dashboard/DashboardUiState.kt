package com.kidz.workouted.presentation.dashboard

import com.kidz.workouted.domain.model.Rank

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val muscleGroupRanks: Map<String, Rank>, // Keep for compatibility if needed, or replace
        val muscleGroupsProgression: Map<String, MuscleGroupProgression>,
        val workoutDates: Set<Long>,
        val weeklyWorkoutsCount: Int,
        val strengthIncreasePercentage: Int,
        val activeEnergyKcal: Int,
        val activeTimeHours: Double
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

data class MuscleGroupProgression(
    val id: String,
    val score: Double,
    val rank: Rank,
    val muscles: List<MuscleProgression>,
    val hasUnseenProgression: Boolean = false
)

data class MuscleProgression(
    val id: String,
    val score: Double,
    val rank: Rank,
    val isRankIncreased: Boolean = false
)
