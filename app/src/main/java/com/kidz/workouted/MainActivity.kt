package com.kidz.workouted

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.kidz.workouted.data.local.WorkoutedDatabase
import com.kidz.workouted.presentation.navigation.MainScreen
import com.kidz.workouted.ui.theme.WorkoutedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var database: WorkoutedDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val exercises = database.workoutDao.getAllExercises().first()
            Log.d("MainActivity", "Exercise count: ${exercises.size}")
            exercises.forEach { Log.d("MainActivity", "Exercise: ${it.name}") }
        }

        enableEdgeToEdge()
        setContent {
            WorkoutedTheme {
                MainScreen()
            }
        }
    }
}
