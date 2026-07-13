package com.kidz.workouted.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

import com.kidz.workouted.R

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding", R.string.app_name)
    object Dashboard : Screen("dashboard", R.string.nav_home, Icons.Default.Home)
    object Log : Screen("log", R.string.nav_log, Icons.Default.History)
    object Stats : Screen("stats", R.string.nav_stats, Icons.Default.Assessment)
    object Social : Screen("social", R.string.nav_social, Icons.Default.People)
    object Settings : Screen("settings?highlightLogin={highlightLogin}", R.string.nav_profile, Icons.Default.Settings) {
        fun createRoute(highlightLogin: Boolean = false) = "settings?highlightLogin=$highlightLogin"
    }
    object AddWorkout : Screen("add_workout?workoutId={workoutId}", R.string.add_workout) {
        fun createRoute(workoutId: Long? = null) = if (workoutId != null) "add_workout?workoutId=$workoutId" else "add_workout"
    }
    object SelectExercise : Screen("select_exercise", R.string.select_exercise)
    object WorkoutDetails : Screen("workout_details/{workoutId}", R.string.workout_details) {
        fun createRoute(workoutId: Long) = "workout_details/$workoutId"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Log,
    Screen.Stats,
    Screen.Settings
)
