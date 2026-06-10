package com.kidz.workouted.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kidz.workouted.presentation.dashboard.DashboardScreen
import com.kidz.workouted.presentation.log.*
import com.kidz.workouted.presentation.stats.StatsScreen
import com.kidz.workouted.presentation.stats.StatsViewModel
import com.kidz.workouted.presentation.settings.SettingsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.remember

import androidx.compose.ui.res.stringResource

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    bottomNavItems.forEach { screen ->
                        IconButton(
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ) {
                            screen.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(screen.titleResId),
                                    tint = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.AddWorkout.createRoute()) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Workout")
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = hiltViewModel())
            }
            composable(Screen.Log.route) {
                LogScreen(
                    viewModel = hiltViewModel(),
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Screen.WorkoutDetails.createRoute(workoutId))
                    }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = hiltViewModel())
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = Screen.AddWorkout.route,
                arguments = listOf(navArgument("workoutId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.AddWorkout.route)
                }
                val viewModel: AddWorkoutViewModel = hiltViewModel(parentEntry)
                
                val workoutIdString = backStackEntry.arguments?.getString("workoutId")
                val workoutId = workoutIdString?.toLongOrNull()
                
                LaunchedEffect(workoutId) {
                    if (workoutId != null) {
                        viewModel.loadWorkout(workoutId)
                    }
                }

                AddWorkoutScreen(
                    viewModel = viewModel,
                    onAddExerciseClick = { navController.navigate(Screen.SelectExercise.route) },
                    onBackClick = { navController.popBackStack() },
                    onFinish = { navController.popBackStack() }
                )
            }
            composable(Screen.SelectExercise.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.AddWorkout.route)
                }
                SelectExerciseScreen(
                    viewModel = hiltViewModel(parentEntry),
                    onBackClick = { navController.popBackStack() },
                    onExerciseSelected = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.WorkoutDetails.route,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                WorkoutDetailsScreen(
                    workoutId = workoutId,
                    viewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate(Screen.AddWorkout.createRoute(id))
                    }
                )
            }
        }
    }
}
