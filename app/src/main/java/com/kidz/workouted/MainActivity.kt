package com.kidz.workouted

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidz.workouted.presentation.navigation.MainScreen
import com.kidz.workouted.ui.theme.WorkoutedTheme
import dagger.hilt.android.AndroidEntryPoint

import com.kidz.workouted.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kidz.workouted.domain.model.AppTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            val themeString by preferencesRepository.appTheme.collectAsState(initial = AppTheme.SYSTEM.name)
            val appTheme = runCatching { AppTheme.valueOf(themeString) }.getOrDefault(AppTheme.SYSTEM)
            
            val userColorString by preferencesRepository.userColor.collectAsState(initial = null)
            
            WorkoutedTheme(theme = appTheme, userColor = userColorString) {
                MainScreen()
            }
        }
    }
}
