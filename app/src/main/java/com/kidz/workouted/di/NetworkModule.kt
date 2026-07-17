package com.kidz.workouted.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kidz.workouted.data.remote.WorkoutedApi
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://workouted.kddz.online:1454/api/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(preferencesRepository: UserPreferencesRepository): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { preferencesRepository.jwtToken.first() }
            val request = chain.request().newBuilder().apply {
                if (!token.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }

        val baseUrlInterceptor = Interceptor { chain ->
            val customServerUrl = runBlocking { preferencesRepository.customServerUrl.first() }
            var request = chain.request()
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
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkoutedApi(retrofit: Retrofit): WorkoutedApi {
        return retrofit.create(WorkoutedApi::class.java)
    }
}
