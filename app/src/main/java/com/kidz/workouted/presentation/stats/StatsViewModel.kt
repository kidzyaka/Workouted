package com.kidz.workouted.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Simulate loading
            _uiState.value = StatsUiState.Success(
                activityData = listOf(
                    ActivityData("M", 0.4f),
                    ActivityData("T", 0.7f),
                    ActivityData("W", 0.3f),
                    ActivityData("T", 0.9f),
                    ActivityData("F", 0.6f),
                    ActivityData("S", 0.2f),
                    ActivityData("S", 0.1f)
                ),
                progressData = listOf(
                    ProgressData("Oct 1", 80f),
                    ProgressData("Oct 8", 82.5f),
                    ProgressData("Oct 15", 85f),
                    ProgressData("Oct 22", 84f),
                    ProgressData("Oct 29", 88f),
                    ProgressData("Nov 5", 92f)
                ),
                muscleBalance = mapOf(
                    "Chest" to 0.8f,
                    "Back" to 0.7f,
                    "Legs" to 0.9f,
                    "Arms" to 0.6f,
                    "Shoulders" to 0.75f
                )
            )
        }
    }
}
