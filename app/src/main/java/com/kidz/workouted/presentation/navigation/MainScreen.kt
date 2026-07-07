package com.kidz.workouted.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onNavigate: (String) -> Unit,
    onAddWorkoutClick: () -> Unit
) {
    val showBottomBar = currentDestination?.route != Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ExpressiveNavigationBar(
                    currentDestination = currentDestination,
                    hasRankUp = hasRankUp,
                    onNavigate = onNavigate,
                    onAddWorkoutClick = onAddWorkoutClick
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
            enterTransition = {
                fadeIn(animationSpec = tween(100))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(100))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(100))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(100))
            }
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

@Composable
fun ExpressiveNavigationBar(
    currentDestination: NavDestination?,
    hasRankUp: Boolean,
    onNavigate: (String) -> Unit,
    onAddWorkoutClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First two items
            bottomNavItems.take(2).forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val weight by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.45f, // More bounce
                        stiffness = 500f      // Slightly less stiff for longer visible bounce
                    ),
                    label = "weight"
                )
                
                NavigationItem(
                    screen = screen,
                    isSelected = isSelected,
                    shouldHighlight = screen.route == Screen.Dashboard.route && hasRankUp && !isSelected,
                    onNavigate = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigate(it)
                    },
                    weight = weight
                )
            }

            // Central Add Button (Wider)
            val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val addScale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = 300f),
                label = "addScale"
            )

            Surface(
                modifier = Modifier
                    .weight(1.3f)
                    .height(64.dp)
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        scaleX = addScale
                        scaleY = addScale
                    },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddWorkoutClick() 
                },
                interactionSource = interactionSource
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Workout",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Last two items
            bottomNavItems.drop(2).forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val weight by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.45f, // More bounce
                        stiffness = 500f      // Slightly less stiff for longer visible bounce
                    ),
                    label = "weight"
                )

                NavigationItem(
                    screen = screen,
                    isSelected = isSelected,
                    shouldHighlight = false,
                    onNavigate = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigate(it)
                    },
                    weight = weight
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavigationItem(
    screen: Screen,
    isSelected: Boolean,
    shouldHighlight: Boolean,
    onNavigate: (String) -> Unit,
    weight: Float
) {
    Surface(
        modifier = Modifier
            .weight(weight)
            .height(64.dp)
            .padding(horizontal = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = { onNavigate(screen.route) }
    ) {
        val highlightColor = MaterialTheme.colorScheme.primary
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (shouldHighlight) {
                        val infiniteTransition = rememberInfiniteTransition(label = "glow")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 0.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        Modifier.drawBehind {
                            drawCircle(
                                color = highlightColor.copy(alpha = alpha),
                                radius = 24.dp.toPx()
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            screen.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(screen.titleResId),
                    modifier = Modifier.size(28.dp),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
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
        Box(modifier = Modifier.fillMaxSize()) {
            ExpressiveNavigationBar(
                currentDestination = null,
                hasRankUp = true,
                onNavigate = {},
                onAddWorkoutClick = {}
            )
            
            LargeFloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 80.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Add Workout",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
