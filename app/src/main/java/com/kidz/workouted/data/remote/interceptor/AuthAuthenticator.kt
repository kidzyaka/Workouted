package com.kidz.workouted.data.remote.interceptor

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val token = tokenManager.jwtToken ?: return null
        
        // If the request already failed with an Authorization header, give up to avoid loops
        if (response.request.header("Authorization") != null) {
            return null
        }
        
        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}
