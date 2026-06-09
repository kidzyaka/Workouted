package com.kidz.workouted.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Log : Screen("log", "Log", Icons.Default.History)
    object Stats : Screen("stats", "Stats", Icons.Default.Assessment)
    object Settings : Screen("settings", "Profile", Icons.Default.Settings)
    object AddWorkout : Screen("add_workout?workoutId={workoutId}", "Add Workout") {
        fun createRoute(workoutId: Long? = null) = if (workoutId != null) "add_workout?workoutId=$workoutId" else "add_workout"
    }
    object SelectExercise : Screen("select_exercise", "Select Exercise")
    object WorkoutDetails : Screen("workout_details/{workoutId}", "Workout Details") {
        fun createRoute(workoutId: Long) = "workout_details/$workoutId"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Log,
    Screen.Stats,
    Screen.Settings
)
