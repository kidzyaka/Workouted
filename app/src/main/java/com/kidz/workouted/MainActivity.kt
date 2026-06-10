package com.kidz.workouted

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidz.workouted.presentation.navigation.MainScreen
import com.kidz.workouted.ui.theme.WorkoutedTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            WorkoutedTheme {
                MainScreen()
            }
        }
    }
}
