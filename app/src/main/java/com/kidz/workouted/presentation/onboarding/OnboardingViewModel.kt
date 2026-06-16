package com.kidz.workouted.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    private val _heightCm = MutableStateFlow(175.0)
    val heightCm: StateFlow<Double> = _heightCm.asStateFlow()

    private val _weightKg = MutableStateFlow(75.0)
    val weightKg: StateFlow<Double> = _weightKg.asStateFlow()

    private val _age = MutableStateFlow(25)
    val age: StateFlow<Int> = _age.asStateFlow()

    fun setHeight(height: Double) {
        _heightCm.value = height
    }

    fun setWeight(weight: Double) {
        _weightKg.value = weight
    }

    fun setAge(age: Int) {
        _age.value = age
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setUserHeightCm(_heightCm.value)
            repository.setUserWeightKg(_weightKg.value)
            repository.setUserAge(_age.value)
            repository.setOnboardingCompleted(true)
        }
    }
}
