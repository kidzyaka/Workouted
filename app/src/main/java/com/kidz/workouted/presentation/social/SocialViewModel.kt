package com.kidz.workouted.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidz.workouted.data.local.dao.WorkoutDao
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.data.remote.model.FriendRequestDto
import com.kidz.workouted.data.remote.model.LeaderboardEntry
import com.kidz.workouted.data.remote.model.OneRepMaxPointDto
import com.kidz.workouted.data.repository.SocialRepository
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kidz.workouted.domain.usecase.CalculateOneRepMaxUseCase

data class SocialUiState(
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val requests: List<FriendRequestDto> = emptyList(),
    val friendsVolumeStats: Map<String, List<OneRepMaxPointDto>> = emptyMap(),
    val userVolumeStats: List<OneRepMaxPointDto> = emptyList(),
    val friendColorOverrides: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userColor: String? = null,
    val userFriendCode: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val workoutDao: WorkoutDao,
    private val calculateOneRepMax: CalculateOneRepMaxUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
        
        // Listen to preferences
        preferencesRepository.friendColorOverrides.onEach { overrides ->
            _uiState.update { it.copy(friendColorOverrides = overrides) }
        }.launchIn(viewModelScope)
        
        preferencesRepository.userColor.onEach { color ->
            _uiState.update { it.copy(userColor = color) }
        }.launchIn(viewModelScope)

        preferencesRepository.friendCode.onEach { code ->
            _uiState.update { it.copy(userFriendCode = code) }
        }.launchIn(viewModelScope)

        preferencesRepository.jwtToken.onEach { token ->
            val loggedIn = !token.isNullOrBlank()
            _uiState.update { it.copy(isLoggedIn = loggedIn) }
            if (loggedIn) {
                loadData()
            }
        }.launchIn(viewModelScope)
    }

    fun loadData() {
        if (!_uiState.value.isLoggedIn) return
        fetchLeaderboard()
        fetchRequests()
        fetchVolumeStats()
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = socialRepository.getLeaderboard()
            if (result.isSuccess) {
                // Sort by total score descending
                val sorted = result.getOrNull()?.sortedByDescending { it.totalScore } ?: emptyList()
                _uiState.update { it.copy(isLoading = false, leaderboard = sorted) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("WORKOUTED_ERR", "Leaderboard error: ", result.exceptionOrNull())
                _uiState.update { it.copy(isLoading = false, error = "Failed to load leaderboard: $errorMsg") }
            }
        }
    }

    fun fetchRequests() {
        viewModelScope.launch {
            val result = socialRepository.getFriendRequests()
            if (result.isSuccess) {
                _uiState.update { it.copy(requests = result.getOrNull() ?: emptyList()) }
            }
        }
    }

    fun sendFriendRequest(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = socialRepository.sendFriendRequest(code)
            _uiState.update { it.copy(isLoading = false) }
            if (result.isFailure) {
                _uiState.update { it.copy(error = "Failed to send request") }
            } else {
                _uiState.update { it.copy(error = "Request sent successfully!") }
            }
        }
    }

    fun acceptRequest(friendshipId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = socialRepository.acceptFriendRequest(friendshipId)
            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.update { it.copy(error = "Failed to accept request") }
            }
        }
    }



    private fun fetchVolumeStats() {
        viewModelScope.launch {
            // Fetch friends data
            val result = socialRepository.getFriendsVolumeStats()
            if (result.isSuccess) {
                _uiState.update { it.copy(friendsVolumeStats = result.getOrNull() ?: emptyMap()) }
            }
            
            // Generate user's own volume data from local DB
            val sets = workoutDao.getAllSets().firstOrNull() ?: emptyList()
            val workouts = workoutDao.getAllWorkouts().firstOrNull() ?: emptyList()
            val workoutMap = workouts.associateBy { it.id }
            
            val userStats = sets.groupBy { it.workoutId }.mapNotNull { (wId, wSets) ->
                val w = workoutMap[wId] ?: return@mapNotNull null
                val totalVolume = wSets.sumOf { 
                    val weightToUse = if (it.weight > 0.0) it.weight else 1.0
                    weightToUse * it.reps 
                }
                OneRepMaxPointDto(timestamp = w.timestamp, oneRm = totalVolume)
            }.sortedBy { it.timestamp }
            
            _uiState.update { it.copy(userVolumeStats = userStats) }
        }
    }

    fun setFriendColor(friendId: Long, colorHex: String) {
        viewModelScope.launch {
            preferencesRepository.setFriendColorOverride(friendId, colorHex)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
