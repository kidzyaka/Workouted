package com.kidz.workouted.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.domain.repository.BackupRepository
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val height: String = "175",
    val weight: String = "75",
    val age: String = "25",
    val language: String = "en",
    val backupSuccess: Boolean? = null,
    val backupError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val repository: UserPreferencesRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.userHeightCm,
            repository.userWeightKg,
            repository.userAge,
            repository.appLanguage
        ) { height, weight, age, language ->
            SettingsUiState(
                height = height.toString(),
                weight = weight.toString(),
                age = age.toString(),
                language = language
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun updateHeight(height: String) {
        val value = height.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setUserHeightCm(value)
        }
    }

    fun updateWeight(weight: String) {
        val value = weight.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setUserWeightKg(value)
        }
    }

    fun updateAge(age: String) {
        val value = age.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.setUserAge(value)
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.setAppLanguage(languageCode)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(false)
        }
    }

    suspend fun getExportJson(): String {
        return backupRepository.exportData()
    }

    fun importFromJson(json: String) {
        viewModelScope.launch {
            val result = backupRepository.importData(json)
            if (result.isSuccess) {
                _uiState.update { it.copy(backupSuccess = true, backupError = null) }
            } else {
                _uiState.update { it.copy(backupSuccess = false, backupError = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearBackupState() {
        _uiState.update { it.copy(backupSuccess = null, backupError = null) }
    }
}
