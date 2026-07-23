package com.kidz.workouted.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.domain.repository.BackupRepository
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kidz.workouted.data.repository.AuthRepository
import com.kidz.workouted.data.repository.SyncRepository

data class SettingsUiState(
    val height: String = "175",
    val weight: String = "75",
    val age: String = "25",
    val language: String = "en",
    val backupSuccess: Boolean? = null,
    val backupError: String? = null,
    val token: String? = null,
    val friendCode: String? = null,
    val userColor: String? = null,
    val isLoading: Boolean = false,
    val authError: String? = null,
    val customServerUrl: String? = null,
    val appTheme: String = com.kidz.workouted.domain.model.AppTheme.SYSTEM.name,
    val showSyncConflictDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val repository: UserPreferencesRepository,
    private val backupRepository: BackupRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.userHeightCm,
            repository.userWeightKg,
            repository.userAge,
            repository.appLanguage,
            repository.jwtToken,
            repository.friendCode,
            repository.userColor,
            repository.customServerUrl,
            repository.appTheme
        ) { values ->
            val height = values[0] as Double
            val weight = values[1] as Double
            val age = values[2] as Int
            val language = values[3] as String
            val token = values[4] as String?
            val friendCode = values[5] as String?
            val userColor = values[6] as String?
            val customServerUrl = values[7] as String?
            val appTheme = values[8] as String

            _uiState.value.copy(
                height = height.toString(),
                weight = weight.toString(),
                age = age.toString(),
                language = language,
                token = token,
                friendCode = friendCode,
                userColor = userColor,
                customServerUrl = customServerUrl,
                appTheme = appTheme
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun setCustomServerUrl(url: String) {
        viewModelScope.launch {
            if (url.isBlank()) {
                repository.setCustomServerUrl(null)
            } else {
                var formatted = url.trim()
                if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                    formatted = "http://$formatted"
                }
                if (!formatted.endsWith("/api/") && !formatted.endsWith("/api")) {
                    if (formatted.endsWith("/")) formatted += "api/"
                    else formatted += "/api/"
                } else if (formatted.endsWith("/api")) {
                    formatted += "/"
                }
                repository.setCustomServerUrl(formatted)
            }
        }
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

    fun updateTheme(themeName: String) {
        viewModelScope.launch {
            repository.setAppTheme(themeName)
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
        _uiState.update { it.copy(backupSuccess = null, backupError = null, authError = null) }
    }

    fun updateUserColor(color: String) {
        viewModelScope.launch { repository.setUserColor(color) }
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            val result = authRepository.login(username, pass)
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    authError = if (result.isFailure) result.exceptionOrNull()?.message ?: "Login failed" else null
                ) 
            }
        }
    }

    fun register(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            val result = authRepository.register(username, pass)
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    authError = if (result.isFailure) result.exceptionOrNull()?.message ?: "Register failed" else null
                ) 
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun pushBackupToCloud(force: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, backupError = null) }
            val data = backupRepository.getBackupData(force)
            val result = syncRepository.pushBackup(data)
            
            if (result.isFailure) {
                val e = result.exceptionOrNull()
                if (e is retrofit2.HttpException && e.code() == 409) {
                    _uiState.update { it.copy(isLoading = false, showSyncConflictDialog = true) }
                    return@launch
                }
            }
            
            if (result.isSuccess) {
                repository.setHasUnsyncedChanges(false)
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    backupSuccess = result.isSuccess,
                    backupError = if (result.isFailure) result.exceptionOrNull()?.message ?: "Push failed" else null
                ) 
            }
        }
    }

    fun dismissSyncConflictDialog() {
        _uiState.update { it.copy(showSyncConflictDialog = false) }
    }

    fun pullBackupFromCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, backupError = null) }
            val result = syncRepository.pullBackup()
            if (result.isSuccess) {
                backupRepository.restoreFromBackupData(result.getOrThrow())
                _uiState.update { it.copy(isLoading = false, backupSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        backupSuccess = false,
                        backupError = result.exceptionOrNull()?.message ?: "Pull failed"
                    ) 
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, authError = null) }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        authError = result.exceptionOrNull()?.message ?: "Failed to delete account"
                    ) 
                }
            }
        }
    }
}
