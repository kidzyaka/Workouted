package com.kidz.workouted.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.presentation.components.MuscleBadge
import com.kidz.workouted.presentation.components.StatCard
import com.kidz.workouted.ui.theme.WorkoutedTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(uiState = uiState)
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Muscle Map",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your progress by muscle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Muscle Map Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState is DashboardUiState.Success) {
                // Simplified positioning for now
                Box(Modifier.fillMaxSize()) {
                    MuscleBadge(
                        "Chest", 
                        uiState.muscleGroupRanks["Chest"] ?: Rank.TREE,
                        Modifier.align(Alignment.Center).offset(y = (-40).dp)
                    )
                    MuscleBadge(
                        "Back", 
                        uiState.muscleGroupRanks["Back"] ?: Rank.TREE,
                        Modifier.align(Alignment.Center).offset(x = (-80).dp, y = 20.dp)
                    )
                    MuscleBadge(
                        "Legs", 
                        uiState.muscleGroupRanks["Legs"] ?: Rank.TREE,
                        Modifier.align(Alignment.Center).offset(y = 80.dp)
                    )
                    MuscleBadge(
                        "Arms", 
                        uiState.muscleGroupRanks["Arms"] ?: Rank.TREE,
                        Modifier.align(Alignment.Center).offset(x = 80.dp, y = 20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO */ }) {
                Text("See History")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState is DashboardUiState.Success) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Active Time",
                        value = uiState.activeTimeHours.toString(),
                        unit = "h",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    StatCard(
                        title = "Calories",
                        value = "${uiState.activeEnergyKcal / 1000}.${(uiState.activeEnergyKcal % 1000) / 100}k",
                        unit = "kcal",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Workouts",
                        value = uiState.weeklyWorkoutsCount.toString(),
                        unit = "this week",
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF80CBC4) // Emerald shade
                    )
                    StatCard(
                        title = "Strength",
                        value = "+${uiState.strengthIncreasePercentage}%",
                        unit = "increase",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        } else if (uiState is DashboardUiState.Loading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
fun DashboardPreview() {
    WorkoutedTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardContent(
                uiState = DashboardUiState.Success(
                    muscleGroupRanks = mapOf(
                        "Chest" to Rank.SILVER,
                        "Back" to Rank.GOLD,
                        "Legs" to Rank.BRONZE,
                        "Arms" to Rank.PLATINUM,
                        "Shoulders" to Rank.TREE
                    ),
                    weeklyWorkoutsCount = 12,
                    strengthIncreasePercentage = 12,
                    activeEnergyKcal = 1800,
                    activeTimeHours = 4.2
                )
            )
        }
    }
}
