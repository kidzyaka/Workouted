package com.kidz.workouted.data.remote.interceptor

import com.kidz.workouted.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    preferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    var jwtToken: String? = null
        private set

    @Volatile
    var customServerUrl: String? = null
        private set

    init {
        preferencesRepository.jwtToken.onEach {
            jwtToken = it
        }.launchIn(scope)

        preferencesRepository.customServerUrl.onEach {
            customServerUrl = it
        }.launchIn(scope)
    }
}
