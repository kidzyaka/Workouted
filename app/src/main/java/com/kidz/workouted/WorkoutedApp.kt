package com.kidz.workouted

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WorkoutedApp : Application() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        
        // Apply saved language preference early to minimize flickering
        MainScope().launch {
            val language = preferencesRepository.appLanguage.first()
            if (AppCompatDelegate.getApplicationLocales().isEmpty) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
            }
        }
    }
}
