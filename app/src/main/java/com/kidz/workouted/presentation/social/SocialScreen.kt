package com.kidz.workouted.presentation.social

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidz.workouted.core.util.LocalizationUtil
import com.kidz.workouted.data.local.entity.ExerciseEntity
import com.kidz.workouted.data.remote.model.FriendRequestDto
import com.kidz.workouted.data.remote.model.LeaderboardEntry
import com.kidz.workouted.data.remote.model.OneRepMaxPointDto
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.presentation.components.StaggeredEntranceItem
import com.kidz.workouted.presentation.settings.ColorSelectionDialog
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.kidz.workouted.R

@Composable
fun SocialScreen(
    viewModel: SocialViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<LeaderboardEntry?>(null) }
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }

    if (!uiState.isLoggedIn) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.social_login_prompt),
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToLogin) {
                Text(stringResource(R.string.action_login))
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 80.dp) // padding for FAB and Nav Bar
        ) {
            StaggeredEntranceItem(index = 0) {
                Column {
                    Text(
                        text = stringResource(R.string.social_server_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.social_server_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            val friendsLeaderboard = uiState.leaderboard.filter { it.friendCode != uiState.userFriendCode }

            StaggeredEntranceItem(index = 1) {
                FriendsLeaderboardSection(leaderboard = friendsLeaderboard)
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredEntranceItem(index = 3) {
                MultiLineVolumeChartSection(
                    friendsStats = uiState.friendsVolumeStats,
                    userStats = uiState.userVolumeStats,
                    leaderboard = friendsLeaderboard,
                    friendColorOverrides = uiState.friendColorOverrides,
                    userColor = uiState.userColor
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredEntranceItem(index = 3) {
                FriendCardsSection(
                    leaderboard = friendsLeaderboard,
                    friendColorOverrides = uiState.friendColorOverrides,
                    onUpdateColor = { friendId, color -> viewModel.setFriendColor(friendId, color) },
                    onFriendClick = { selectedFriend = it }
                )
            }

            if (uiState.requests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                StaggeredEntranceItem(index = 4) {
                    FriendRequestsSection(
                        requests = uiState.requests,
                        onAccept = { viewModel.acceptRequest(it) },
                        onReject = { viewModel.rejectRequest(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        if (showAddFriendDialog) {
            AddFriendDialog(
                onAdd = { code -> 
                    viewModel.sendFriendRequest(code)
                    showAddFriendDialog = false
                },
                onDismiss = { showAddFriendDialog = false }
            )
        }

        selectedFriend?.let { friend ->
            FriendDetailsDialog(
                friend = friend,
                customColor = uiState.friendColorOverrides[friend.friendId],
                stats = uiState.friendsVolumeStats[friend.friendId.toString()] ?: emptyList(),
                onDismiss = { selectedFriend = null },
                onRemove = { showRemoveConfirmDialog = true }
            )
        }

        if (showRemoveConfirmDialog && selectedFriend != null) {
            RemoveFriendConfirmationDialog(
                onConfirm = {
                    viewModel.removeFriend(selectedFriend!!.friendId)
                    showRemoveConfirmDialog = false
                    selectedFriend = null
                },
                onDismiss = { showRemoveConfirmDialog = false }
            )
        }

        FloatingActionButton(
            onClick = { showAddFriendDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp) // Offset above the navigation bar
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.add_friend))
        }
    }
}

@Composable
fun FriendRequestsSection(requests: List<FriendRequestDto>, onAccept: (Long) -> Unit, onReject: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.friend_requests),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            requests.forEach { req ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonOutline, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(req.requesterUsername, fontWeight = FontWeight.Bold)
                        Text(req.requesterCode, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { onReject(req.friendshipId) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_reject), tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { onAccept(req.friendshipId) }) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_accept), tint = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsLeaderboardSection(leaderboard: List<LeaderboardEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.friends_leaderboard),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (leaderboard.isEmpty()) {
                Text(stringResource(R.string.no_friends_yet), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                leaderboard.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${index + 1}",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(40.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.username, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.score_prefix) + entry.totalScore.toInt(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (index < leaderboard.size - 1) {
                        Divider(modifier = Modifier.padding(start = 40.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiLineProgressChartSection(
    friendsStats: Map<String, List<OneRepMaxPointDto>>,
    userStats: List<OneRepMaxPointDto>,
    exercises: List<ExerciseEntity>,
    selectedExerciseId: Long?,
    onExerciseSelected: (Long) -> Unit,
    leaderboard: List<LeaderboardEntry>,
    friendColorOverrides: Map<Long, String>,
    userColor: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "1RM Progress Overlay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            val selectedExerciseName = exercises.find { it.id == selectedExerciseId }?.name ?: stringResource(R.string.select_exercise)
            val context = LocalContext.current

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = if (selectedExerciseName != stringResource(R.string.select_exercise)) LocalizationUtil.getLocalizedName(context, selectedExerciseName) else selectedExerciseName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    exercises.forEach { ex ->
                        DropdownMenuItem(
                            text = { Text(LocalizationUtil.getLocalizedName(context, ex.name)) },
                            onClick = {
                                onExerciseSelected(ex.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val defaultUserColor = Color(android.graphics.Color.parseColor(userColor ?: "#E57373"))

            // Plot all data
            val allDataSets = mutableListOf<Pair<Color, List<OneRepMaxPointDto>>>()
            if (userStats.isNotEmpty()) {
                allDataSets.add(defaultUserColor to userStats)
            }

            friendsStats.forEach { (friendIdStr, stats) ->
                val friendId = friendIdStr.toLong()
                val friend = leaderboard.find { it.friendId == friendId }
                val hexColor = friendColorOverrides[friendId] ?: friend?.defaultColor ?: "#888888"
                val color = try { Color(android.graphics.Color.parseColor(hexColor)) } catch (e: Exception) { Color.Gray }
                if (stats.isNotEmpty()) {
                    allDataSets.add(color to stats)
                }
            }

            val animationProgress = remember { Animatable(0f) }
            LaunchedEffect(allDataSets) {
                animationProgress.snapTo(0f)
                animationProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
            }

            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }

            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceAtLeast(1f).coerceAtMost(10f)
                        offsetX += pan.x
                        
                        // Limit offset so we don't pan too far off screen
                        val maxOffset = 0f
                        val minOffset = -size.width * (scale - 1)
                        offsetX = offsetX.coerceIn(minOffset, maxOffset)
                    }
                }
            ) {
                if (allDataSets.isEmpty()) {
                    val valY = size.height / 2
                    drawCircle(defaultUserColor, radius = 6.dp.toPx(), center = Offset(size.width / 2, valY))
                } else {
                    val allPoints = allDataSets.flatMap { it.second }
                    val minTime = allPoints.minOf { it.timestamp }
                    val maxTime = allPoints.maxOf { it.timestamp }.coerceAtLeast(minTime + 86400000)
                    val minVal = allPoints.minOf { it.oneRm } * 0.9
                    val maxVal = allPoints.maxOf { it.oneRm }.coerceAtLeast(1.0)
                    val timeRange = (maxTime - minTime).toDouble()
                    val valRange = (maxVal - minVal).coerceAtLeast(1.0)

                    allDataSets.forEach { (color, stats) ->
                        val sortedStats = stats.sortedBy { it.timestamp }
                        val points = sortedStats.map { stat ->
                            val targetY = size.height - (((stat.oneRm - minVal) / valRange) * size.height).toFloat()
                            val animatedY = size.height - (size.height - targetY) * animationProgress.value
                            val baseX = (((stat.timestamp - minTime).toDouble() / timeRange) * size.width).toFloat()
                            Offset(
                                x = (baseX * scale) + offsetX,
                                y = animatedY
                            )
                        }

                        if (points.size == 1) {
                            drawCircle(color, radius = 6.dp.toPx(), center = points.first())
                        } else {
                            val strokePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val cx = (p1.x + p2.x) / 2
                                    cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(strokePath)
                                lineTo(points.last().x, size.height)
                                lineTo(points.first().x, size.height)
                                close()
                            }

                            val gradient = Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = 0.5f * animationProgress.value), Color.Transparent),
                                startY = points.minOf { it.y },
                                endY = size.height
                            )

                            drawPath(path = fillPath, brush = gradient)
                            drawPath(
                                path = strokePath,
                                color = color,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            
                            points.forEach { point ->
                                drawCircle(Color.White, radius = 4.dp.toPx(), center = point)
                                drawCircle(color, radius = 2.dp.toPx(), center = point)
                            }
                        }
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(defaultUserColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.you), style = MaterialTheme.typography.labelSmall)
                }
                friendsStats.keys.take(3).forEach { friendIdStr ->
                    val friendId = friendIdStr.toLong()
                    val friend = leaderboard.find { it.friendId == friendId }
                    val hexColor = friendColorOverrides[friendId] ?: friend?.defaultColor ?: "#888888"
                    val color = try { Color(android.graphics.Color.parseColor(hexColor)) } catch (e: Exception) { Color.Gray }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(friend?.username ?: stringResource(R.string.friend), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCardsSection(
    leaderboard: List<LeaderboardEntry>,
    friendColorOverrides: Map<Long, String>,
    onUpdateColor: (Long, String) -> Unit,
    onFriendClick: (LeaderboardEntry) -> Unit
) {
    Column {
        Text(
            stringResource(R.string.friend_profiles),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        leaderboard.filter { it.friendId != -1L }.forEach { friend ->
            FriendCard(
                friend = friend,
                customColor = friendColorOverrides[friend.friendId],
                onUpdateColor = { onUpdateColor(friend.friendId, it) },
                onClick = { onFriendClick(friend) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FriendCard(
    friend: LeaderboardEntry,
    customColor: String?,
    onUpdateColor: (String) -> Unit,
    onClick: () -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }
    val displayColorHex = customColor ?: friend.defaultColor ?: "#888888"
    val displayColor = try { Color(android.graphics.Color.parseColor(displayColorHex)) } catch (e: Exception) { Color.Gray }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(displayColor), contentAlignment = Alignment.Center) {
                    Text(friend.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(friend.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.code_prefix) + friend.friendCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showColorDialog = true }) {
                    Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.change_color), tint = displayColor)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.height_short_prefix) + (friend.height ?: "?") + " cm", style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.weight_short_prefix) + (friend.weight ?: "?") + " kg", style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.age_prefix) + (friend.age ?: "?"), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Muscle Ranks Grid
            val ranks = friend.muscleScores.entries.toList()
            if (ranks.isNotEmpty()) {
                val context = LocalContext.current
                Text(stringResource(R.string.muscle_ranks), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                for (i in ranks.indices step 2) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val r1 = ranks[i]
                        RankBadge(name = LocalizationUtil.getLocalizedName(context, r1.key), rank = r1.value.rank, modifier = Modifier.weight(1f))
                        if (i + 1 < ranks.size) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val r2 = ranks[i + 1]
                            RankBadge(name = LocalizationUtil.getLocalizedName(context, r2.key), rank = r2.value.rank, modifier = Modifier.weight(1f))
                        } else {
                            Spacer(modifier = Modifier.weight(1f).padding(start = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showColorDialog) {
        ColorSelectionDialog(
            currentColor = displayColorHex,
            onColorSelected = { 
                onUpdateColor(it)
                showColorDialog = false 
            },
            onDismiss = { showColorDialog = false }
        )
    }
}

@Composable
fun RankBadge(name: String, rank: String, modifier: Modifier = Modifier) {
    val rankObj = Rank.entries.find { it.name.equals(rank, ignoreCase = true) } ?: Rank.WOOD
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = rankObj.color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.labelSmall)
            Text(rank, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rankObj.color)
        }
    }
}

@Composable
fun AddFriendDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_friend)) },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text(stringResource(R.string.friend_code)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onAdd(code) }, enabled = code.length == 6) {
                Text(stringResource(R.string.send_request))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun MultiLineVolumeChartSection(
    friendsStats: Map<String, List<OneRepMaxPointDto>>,
    userStats: List<OneRepMaxPointDto>,
    leaderboard: List<LeaderboardEntry>,
    friendColorOverrides: Map<Long, String>,
    userColor: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(id = R.string.strength_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            val defaultUserColor = Color(android.graphics.Color.parseColor(userColor ?: "#E57373"))

            // Plot all data
            val allDataSets = mutableListOf<Pair<Color, List<OneRepMaxPointDto>>>()
            if (userStats.isNotEmpty()) {
                allDataSets.add(defaultUserColor to userStats)
            }

            friendsStats.forEach { (friendIdStr, stats) ->
                val friendId = friendIdStr.toLong()
                val friend = leaderboard.find { it.friendId == friendId }
                val hexColor = friendColorOverrides[friendId] ?: friend?.defaultColor ?: "#888888"
                val color = try { Color(android.graphics.Color.parseColor(hexColor)) } catch (e: Exception) { Color.Gray }
                if (stats.isNotEmpty()) {
                    allDataSets.add(color to stats)
                }
            }

            val animationProgress = remember { Animatable(0f) }
            LaunchedEffect(allDataSets) {
                animationProgress.snapTo(0f)
                animationProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
            }

            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }

            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceAtLeast(1f).coerceAtMost(10f)
                        offsetX += pan.x
                        
                        val maxOffset = 0f
                        val minOffset = -size.width * (scale - 1)
                        offsetX = offsetX.coerceIn(minOffset, maxOffset)
                    }
                }
            ) {
                if (allDataSets.isEmpty()) {
                    val valY = size.height / 2
                    drawCircle(defaultUserColor, radius = 6.dp.toPx(), center = Offset(size.width / 2, valY))
                } else {
                    val allPoints = allDataSets.flatMap { it.second }
                    val minTime = allPoints.minOf { it.timestamp }
                    val maxTime = allPoints.maxOf { it.timestamp }.coerceAtLeast(minTime + 86400000)
                    val minVal = allPoints.minOf { it.oneRm } * 0.9
                    val maxVal = allPoints.maxOf { it.oneRm }.coerceAtLeast(1.0)
                    val timeRange = (maxTime - minTime).toDouble()
                    val valRange = (maxVal - minVal).coerceAtLeast(1.0)

                    allDataSets.forEach { (color, stats) ->
                        val sortedStats = stats.sortedBy { it.timestamp }
                        val points = sortedStats.map { stat ->
                            val targetY = size.height - (((stat.oneRm - minVal) / valRange) * size.height).toFloat()
                            val animatedY = size.height - (size.height - targetY) * animationProgress.value
                            val baseX = (((stat.timestamp - minTime).toDouble() / timeRange) * size.width).toFloat()
                            Offset(
                                x = (baseX * scale) + offsetX,
                                y = animatedY
                            )
                        }

                        if (points.size == 1) {
                            drawCircle(color, radius = 6.dp.toPx(), center = points.first())
                        } else {
                            val strokePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val cx = (p1.x + p2.x) / 2
                                    cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(strokePath)
                                lineTo(points.last().x, size.height)
                                lineTo(points.first().x, size.height)
                                close()
                            }

                            val gradient = Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = 0.5f * animationProgress.value), Color.Transparent),
                                startY = points.minOf { it.y },
                                endY = size.height
                            )

                            drawPath(path = fillPath, brush = gradient)
                            drawPath(
                                path = strokePath,
                                color = color,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            
                            points.forEach { point ->
                                drawCircle(Color.White, radius = 4.dp.toPx(), center = point)
                                drawCircle(color, radius = 2.dp.toPx(), center = point)
                            }
                        }
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(defaultUserColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.you), style = MaterialTheme.typography.labelSmall)
                }
                friendsStats.keys.take(3).forEach { friendIdStr ->
                    val friendId = friendIdStr.toLong()
                    val friend = leaderboard.find { it.friendId == friendId }
                    val hexColor = friendColorOverrides[friendId] ?: friend?.defaultColor ?: "#888888"
                    val color = try { Color(android.graphics.Color.parseColor(hexColor)) } catch (e: Exception) { Color.Gray }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(friend?.username ?: stringResource(R.string.friend), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendDetailsDialog(
    friend: LeaderboardEntry,
    customColor: String?,
    stats: List<OneRepMaxPointDto>,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    val displayColorHex = customColor ?: friend.defaultColor ?: "#888888"
    val displayColor = try { Color(android.graphics.Color.parseColor(displayColorHex)) } catch (e: Exception) { Color.Gray }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f).padding(vertical = 16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(displayColor), contentAlignment = Alignment.Center) {
                    Text(friend.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(friend.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.code_prefix) + friend.friendCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text(stringResource(R.string.height_short_prefix) + (friend.height ?: "?") + " cm", style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.weight_short_prefix) + (friend.weight ?: "?") + " kg", style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.age_prefix) + (friend.age ?: "?"), style = MaterialTheme.typography.bodySmall)
                }
                
                val ranks = friend.muscleScores.entries.toList()
                if (ranks.isNotEmpty()) {
                    val context = LocalContext.current
                    Text(stringResource(R.string.muscle_ranks), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    for (i in ranks.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val r1 = ranks[i]
                            RankBadge(name = LocalizationUtil.getLocalizedName(context, r1.key), rank = r1.value.rank, modifier = Modifier.weight(1f))
                            if (i + 1 < ranks.size) {
                                Spacer(modifier = Modifier.width(8.dp))
                                val r2 = ranks[i + 1]
                                RankBadge(name = LocalizationUtil.getLocalizedName(context, r2.key), rank = r2.value.rank, modifier = Modifier.weight(1f))
                            } else {
                                Spacer(modifier = Modifier.weight(1f).padding(start = 8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                if (stats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MultiLineVolumeChartSection(
                        friendsStats = mapOf(friend.friendId.toString() to stats),
                        userStats = emptyList(),
                        leaderboard = listOf(friend),
                        friendColorOverrides = mapOf(friend.friendId to displayColorHex),
                        userColor = null
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        dismissButton = {
            Button(
                onClick = onRemove,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.remove_friend))
            }
        }
    )
}

@Composable
fun RemoveFriendConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var timeLeft by remember { mutableIntStateOf(3) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.remove_friend_confirm_title)) },
        text = { Text(stringResource(R.string.remove_friend_confirm_text)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = timeLeft == 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (timeLeft > 0) stringResource(R.string.action_confirm_timer, timeLeft) 
                           else stringResource(R.string.action_confirm)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
