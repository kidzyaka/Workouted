package com.kidz.workouted.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import com.kidz.workouted.presentation.components.StatCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kidz.workouted.R
import com.kidz.workouted.core.util.LocalizationUtil
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.presentation.components.MuscleBadge
import com.kidz.workouted.presentation.components.MuscleProgressionDialog
import com.kidz.workouted.presentation.components.StaggeredEntranceItem
import com.kidz.workouted.ui.theme.WorkoutedTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState, 
        onRankUpSeen = { groupId, newRank -> viewModel.onRankUpSeen(groupId, newRank) },
        onMuscleGroupSeen = { progression -> viewModel.onMuscleGroupSeen(progression) }
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onRankUpSeen: (String, Rank) -> Unit,
    onMuscleGroupSeen: (MuscleGroupProgression) -> Unit
) {
    val context = LocalContext.current
    var selectedGroupProgression by remember { mutableStateOf<MuscleGroupProgression?>(null) }
    var selectedGroupName by remember { mutableStateOf("") }
    
    var currentRankUp by remember { mutableStateOf<RankUpData?>(null) }

    val greeting = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_day
            in 18..21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
    }

    val warriorTitle = (uiState as? DashboardUiState.Success)?.greetingTitleResId ?: R.string.title_warrior

    LaunchedEffect(uiState) {
        if (uiState is DashboardUiState.Success) {
            val nextRankUp = uiState.rankUps.firstOrNull()
            if (nextRankUp != null) {
                currentRankUp = nextRankUp
            }
        }
    }

    currentRankUp?.let { rankUp ->
        RankUpOverlay(
            groupName = LocalizationUtil.getLocalizedName(context, rankUp.groupId),
            newRank = rankUp.newRank,
            onFinished = {
                onRankUpSeen(rankUp.groupId, rankUp.newRank)
                currentRankUp = null
            }
        )
    }

    if (selectedGroupProgression != null) {
        MuscleProgressionDialog(
            progression = selectedGroupProgression!!,
            localizedGroupName = selectedGroupName,
            onDismiss = { selectedGroupProgression = null },
            onSeen = {
                (uiState as? DashboardUiState.Success)?.muscleGroupsProgression?.get(selectedGroupProgression?.id)?.let {
                    onMuscleGroupSeen(it)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Section
            StaggeredEntranceItem(index = 0) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val today = remember { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM")) }
                            Text(
                                text = today,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${stringResource(greeting)} ${stringResource(warriorTitle)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is DashboardUiState.Success) {
                StaggeredEntranceItem(index = 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Workouts",
                            value = uiState.weeklyWorkoutsCount.toString(),
                            unit = "this week",
                            icon = Icons.Default.FitnessCenter,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Progression",
                            value = "${uiState.strengthIncreasePercentage}%",
                            unit = "vs last",
                            icon = Icons.Default.TrendingUp,
                            trend = if (uiState.strengthIncreasePercentage > 0) "+${uiState.strengthIncreasePercentage}%" else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                StaggeredEntranceItem(index = 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Active Energy",
                            value = uiState.activeEnergyKcal.toString(),
                            unit = "kcal",
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Active Time",
                            value = String.format(Locale.US, "%.1f", uiState.activeTimeHours),
                            unit = "hours",
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            StaggeredEntranceItem(index = 1) {
                Text(
                    text = stringResource(R.string.muscle_map),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Muscle Map with Body Silhouette
            StaggeredEntranceItem(index = 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.shapes.extraLarge
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.body_silhouette),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 24.dp),
                        alpha = 0.3f,
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )

                    if (uiState is DashboardUiState.Success) {
                        Box(Modifier.fillMaxSize()) {
                            // BACK (Center, high)
                            MuscleBadge(
                                LocalizationUtil.getLocalizedName(context, "group_back"),
                                uiState.muscleGroupRanks["group_back"] ?: Rank.WOOD,
                                Modifier.align(BiasAlignment(-0f, -0.63f)),
                                hasUnseenProgression = uiState.muscleGroupsProgression["group_back"]?.hasUnseenProgression ?: false,
                                onClick = {
                                    selectedGroupProgression =
                                        uiState.muscleGroupsProgression["group_back"]
                                    selectedGroupName =
                                        LocalizationUtil.getLocalizedName(context, "group_back")
                                }
                            )
                            // SHOULDERS (Sides)
                            MuscleBadge(
                                LocalizationUtil.getLocalizedName(context, "group_shoulders"),
                                uiState.muscleGroupRanks["group_shoulders"] ?: Rank.WOOD,
                                Modifier.align(BiasAlignment(0.35f, -0.55f)),
                                hasUnseenProgression = uiState.muscleGroupsProgression["group_shoulders"]?.hasUnseenProgression ?: false,
                                onClick = {
                                    selectedGroupProgression =
                                        uiState.muscleGroupsProgression["group_shoulders"]
                                    selectedGroupName =
                                        LocalizationUtil.getLocalizedName(context, "group_shoulders")
                                }
                            )
                            // CHEST (Center, under Back)
                            MuscleBadge(
                                LocalizationUtil.getLocalizedName(context, "group_chest"),
                                uiState.muscleGroupRanks["group_chest"] ?: Rank.WOOD,
                                Modifier.align(BiasAlignment(-0.15f, -0.45f)),
                                hasUnseenProgression = uiState.muscleGroupsProgression["group_chest"]?.hasUnseenProgression ?: false,
                                onClick = {
                                    selectedGroupProgression =
                                        uiState.muscleGroupsProgression["group_chest"]
                                    selectedGroupName =
                                        LocalizationUtil.getLocalizedName(context, "group_chest")
                                }
                            )
                            // ARMS (Left side)
                            MuscleBadge(
                                LocalizationUtil.getLocalizedName(context, "group_arms"),
                                uiState.muscleGroupRanks["group_arms"] ?: Rank.WOOD,
                                Modifier.align(BiasAlignment(-0.40f, -0.15f)),
                                hasUnseenProgression = uiState.muscleGroupsProgression["group_arms"]?.hasUnseenProgression ?: false,
                                onClick = {
                                    selectedGroupProgression =
                                        uiState.muscleGroupsProgression["group_arms"]
                                    selectedGroupName =
                                        LocalizationUtil.getLocalizedName(context, "group_arms")
                                }
                            )
                            // CORE
                            MuscleBadge(
                                LocalizationUtil.getLocalizedName(context, "group_core"),
                                uiState.muscleGroupRanks["group_core"] ?: Rank.WOOD,
                                Modifier.align(BiasAlignment(0f, -0.20f)),
                                hasUnseenProgression = uiState.muscleGroupsProgression["group_core"]?.hasUnseenProgression ?: false,
                                onClick = {
                                    selectedGroupProgression =
                                        uiState.muscleGroupsProgression["group_core"]
                                    selectedGroupName =
                                        LocalizationUtil.getLocalizedName(context, "group_core")
                                }
                            )
                            // LEGS (Lower body)
                            MuscleBadge(
                                LocalizationUtil.getLocalizedName(context, "group_legs"),
                                uiState.muscleGroupRanks["group_legs"] ?: Rank.WOOD,
                                Modifier.align(BiasAlignment(0f, 0.65f)),
                                hasUnseenProgression = uiState.muscleGroupsProgression["group_legs"]?.hasUnseenProgression ?: false,
                                onClick = {
                                    selectedGroupProgression =
                                        uiState.muscleGroupsProgression["group_legs"]
                                    selectedGroupName =
                                        LocalizationUtil.getLocalizedName(context, "group_legs")
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            StaggeredEntranceItem(index = 3) {
                when (uiState) {
                    is DashboardUiState.Success -> {
                        WorkoutCalendar(uiState.workoutDates)
                    }
                    is DashboardUiState.Loading -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // Space for bottom bar
        }

        currentRankUp?.let { rankUp ->
            RankUpOverlay(
                groupName = LocalizationUtil.getLocalizedName(context, rankUp.groupId),
                newRank = rankUp.newRank,
                onFinished = {
                    onRankUpSeen(rankUp.groupId, rankUp.newRank)
                    currentRankUp = null
                }
            )
        }
    }
}

@Composable
fun WorkoutCalendar(workoutDates: Set<Long>) {
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.now()) }
    val daysInMonth = currentMonth.lengthOfMonth()
    
    val firstDayOfWeek = java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
    val startOffset = (firstDayOfMonth.value - firstDayOfWeek.value + 7) % 7
    
    val workoutLocalDates = remember(workoutDates) {
        workoutDates.map { 
            java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate() 
        }.toSet()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + " " + currentMonth.year,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekdays Header
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until 7) {
                    val day = firstDayOfWeek.plus(i.toLong())
                    Text(
                        text = day.getDisplayName(java.time.format.TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Grid
            val rows = (daysInMonth + startOffset + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 7) {
                        val dayNum = row * 7 + col - startOffset + 1
                        if (dayNum in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNum)
                            val hasWorkout = workoutLocalDates.contains(date)
                            val isToday = date == java.time.LocalDate.now()
                            val workoutYesterday = workoutLocalDates.contains(date.minusDays(1)) && col > 0
                            val workoutTomorrow = workoutLocalDates.contains(date.plusDays(1)) && col < 6

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background Streak Pill
                                if (hasWorkout) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(0.7f)
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                                    topStart = if (workoutYesterday) 0.dp else 100.dp,
                                                    bottomStart = if (workoutYesterday) 0.dp else 100.dp,
                                                    topEnd = if (workoutTomorrow) 0.dp else 100.dp,
                                                    bottomEnd = if (workoutTomorrow) 0.dp else 100.dp
                                                )
                                            )
                                    )
                                }

                                // Selection Circle
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(0.7f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (hasWorkout || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isToday -> MaterialTheme.colorScheme.onPrimary
                                            hasWorkout -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    WorkoutedTheme {
        DashboardContent(
            uiState = DashboardUiState.Success(
                muscleGroupRanks = mapOf(
                    "group_chest" to Rank.SILVER,
                    "group_back" to Rank.GOLD,
                    "group_legs" to Rank.BRONZE,
                    "group_arms" to Rank.PLATINUM,
                    "group_shoulders" to Rank.DIAMOND,
                    "group_core" to Rank.ELITE
                ),
                muscleGroupsProgression = mapOf(
                    "group_chest" to MuscleGroupProgression("group_chest", 120.0, Rank.SILVER, emptyList(), false),
                    "group_back" to MuscleGroupProgression("group_back", 180.0, Rank.GOLD, emptyList(), false),
                    "group_legs" to MuscleGroupProgression("group_legs", 70.0, Rank.BRONZE, emptyList(), false),
                    "group_arms" to MuscleGroupProgression("group_arms", 250.0, Rank.PLATINUM, emptyList(), false),
                    "group_shoulders" to MuscleGroupProgression("group_shoulders", 450.0, Rank.DIAMOND, emptyList(), false),
                    "group_core" to MuscleGroupProgression("group_core", 600.0, Rank.ELITE, emptyList(), false)
                ),
                workoutDates = setOf(System.currentTimeMillis()),
                weeklyWorkoutsCount = 12,
                strengthIncreasePercentage = 12,
                activeEnergyKcal = 1800,
                activeTimeHours = 4.2,
                greetingTitleResId = R.string.title_athlete,
                rankUps = emptyList()
            ),
            onRankUpSeen = { _, _ -> },
            onMuscleGroupSeen = { }
        )
    }
}
