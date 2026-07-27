package com.kidz.workouted.data.remote.interceptor

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BaseUrlInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    companion object {
        const val BASE_URL = "https://workouted.kddz.ru:1454/api/"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val customServerUrl = tokenManager.customServerUrl
        
        if (!customServerUrl.isNullOrBlank()) {
            val requestUrlStr = request.url.toString()
            if (requestUrlStr.startsWith(BASE_URL)) {
                val newUrlStr = requestUrlStr.replaceFirst(BASE_URL, customServerUrl)
                val newUrl = newUrlStr.toHttpUrlOrNull()
                if (newUrl != null) {
                    request = request.newBuilder().url(newUrl).build()
                }
            }
        }
        return chain.proceed(request)
    }
}
