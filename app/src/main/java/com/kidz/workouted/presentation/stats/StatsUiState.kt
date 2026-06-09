package com.kidz.workouted.presentation.stats

data class ActivityData(val day: String, val value: Float)
data class ProgressData(val date: String, val value: Float)

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(
        val activityData: List<ActivityData>,
        val progressData: List<ProgressData>,
        val muscleBalance: Map<String, Float> // 0.0 to 1.0
    ) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}
