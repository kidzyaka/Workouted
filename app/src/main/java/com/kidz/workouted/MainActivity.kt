package com.kidz.workouted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidz.workouted.presentation.navigation.MainScreen
import com.kidz.workouted.ui.theme.WorkoutedTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.kidz.workouted.data.local.DatabaseInitializer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch(Dispatchers.IO) {
            databaseInitializer.seedIfNeeded()
        }

        enableEdgeToEdge()
        setContent {
            WorkoutedTheme {
                MainScreen()
            }
        }
    }
}
