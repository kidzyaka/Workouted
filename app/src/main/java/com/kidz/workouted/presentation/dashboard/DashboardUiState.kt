package com.kidz.workouted.presentation.dashboard

import com.kidz.workouted.domain.model.Rank

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val muscleGroupRanks: Map<String, Rank>,
        val weeklyWorkoutsCount: Int,
        val strengthIncreasePercentage: Int,
        val activeEnergyKcal: Int,
        val activeTimeHours: Double
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
