package com.kidz.workouted.presentation.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kidz.workouted.domain.repository.UserPreferencesRepository
import com.kidz.workouted.presentation.dashboard.DashboardScreen
import com.kidz.workouted.presentation.log.*
import com.kidz.workouted.presentation.onboarding.OnboardingScreen
import com.kidz.workouted.presentation.settings.SettingsScreen
import com.kidz.workouted.presentation.settings.SettingsViewModel
import com.kidz.workouted.presentation.stats.StatsScreen
import com.kidz.workouted.ui.theme.WorkoutedTheme

@Composable
fun MainScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val dashboardViewModel: com.kidz.workouted.presentation.dashboard.DashboardViewModel = hiltViewModel()
    val repository: UserPreferencesRepository = settingsViewModel.repository
    val isOnboardingCompleted by repository.isOnboardingCompleted.collectAsState(initial = null)

    if (isOnboardingCompleted == null) {
        // Still loading preferences
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    val hasRankUp = (dashboardUiState as? com.kidz.workouted.presentation.dashboard.DashboardUiState.Success)?.rankUps?.isNotEmpty() == true

    val startDestination = if (isOnboardingCompleted == true) Screen.Dashboard.route else Screen.Onboarding.route

    MainContent(
        navController = navController,
        currentDestination = currentDestination,
        startDestination = startDestination,
        hasRankUp = hasRankUp,
        dashboardViewModel = dashboardViewModel,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onAddWorkoutClick = {
            navController.navigate(Screen.AddWorkout.createRoute())
        }
    )
}

@Composable
fun MainContent(
    navController: NavHostController,
    currentDestination: NavDestination?,
    startDestination: String,
    hasRankUp: Boolean,
    dashboardViewModel: com.kidz.workouted.presentation.dashboard.DashboardViewModel,
    onNavigate: (String) -> Unit,
    onAddWorkoutClick: () -> Unit
) {
    val showBottomBar = currentDestination?.route != Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar(
                    actions = {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            val shouldHighlight = screen.route == Screen.Dashboard.route && hasRankUp && !isSelected
                            
                            IconButton(
                                onClick = { onNavigate(screen.route) },
                                modifier = Modifier.size(56.dp)
                            ) {
                                screen.icon?.let { icon ->
                                    Box(contentAlignment = Alignment.Center) {
                                        if (shouldHighlight) {
                                            val infiniteTransition = rememberInfiniteTransition(label = "glow")
                                            val alpha by infiniteTransition.animateFloat(
                                                initialValue = 0.2f,
                                                targetValue = 0.8f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(1000),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "alpha"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                                            )
                                        }
                                        
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = stringResource(screen.titleResId),
                                            modifier = Modifier.size(30.dp),
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = onAddWorkoutClick,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "Add Workout",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = hiltViewModel(),
                    onComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WorkoutedTheme {
        MainContent(
            navController = rememberNavController(),
            currentDestination = null,
            startDestination = Screen.Dashboard.route,
            hasRankUp = true,
            dashboardViewModel = hiltViewModel(),
            onNavigate = {},
            onAddWorkoutClick = {}
        )
    }
}
