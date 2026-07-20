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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import com.kidz.workouted.data.worker.SyncWorker

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueue(syncRequest)

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
