package com.kidz.workouted.presentation.stats

data class ActivityData(val day: String, val value: Float)
data class ProgressData(val date: String, val value: Float)

data class MuscleProgression(
    val name: String,
    val currentScore: Float
)

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(
        val activityData: List<ActivityData>,
        val progressData: List<ProgressData>,
        val muscleBalance: Map<String, Float>, // Normalized 0.0 to 1.0 for Radar
        val muscleProgression: List<MuscleProgression> = emptyList() // Raw scores for progress bars
    ) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}
