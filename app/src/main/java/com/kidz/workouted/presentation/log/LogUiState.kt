package com.kidz.workouted.presentation.log

import com.kidz.workouted.domain.model.Workout

sealed class LogUiState {
    object Loading : LogUiState()
    data class Success(val workouts: List<Workout>) : LogUiState()
    data class Error(val message: String) : LogUiState()
}
