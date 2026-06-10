package com.kidz.workouted

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.kidz.workouted.data.local.WorkoutedDatabase
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import com.kidz.workouted.presentation.navigation.MainScreen
import com.kidz.workouted.ui.theme.WorkoutedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var database: WorkoutedDatabase

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val language = preferencesRepository.appLanguage.first()
            if (AppCompatDelegate.getApplicationLocales().isEmpty) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
            }
            
            val exercises = database.workoutDao.getAllExercises().first()
            Log.d("MainActivity", "Exercise count: ${exercises.size}")
        }

        enableEdgeToEdge()
        setContent {
            WorkoutedTheme {
                MainScreen()
            }
        }
    }
}
