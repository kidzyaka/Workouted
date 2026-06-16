package com.kidz.workouted.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kidz.workouted.R
import com.kidz.workouted.domain.model.Rank
import com.kidz.workouted.presentation.dashboard.MuscleGroupProgression
import com.kidz.workouted.presentation.dashboard.MuscleProgression

import androidx.compose.ui.tooling.preview.Preview
import com.kidz.workouted.ui.theme.WorkoutedTheme

import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun MuscleProgressionDialog(
    progression: MuscleGroupProgression,
    localizedGroupName: String,
    onDismiss: () -> Unit,
    onSeen: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onSeen()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = {
            Text(
                text = localizedGroupName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.group_progression),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressionScale(
                        score = progression.score,
                        currentRank = progression.rank
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                }
                
                item {
                    Text(
                        text = stringResource(R.string.individual_muscles),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(progression.muscles) { muscle ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = muscle.id.replace("muscle_", "").replace("_", " ").uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            if (muscle.isRankIncreased) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(muscle.rank.color)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        ProgressionScale(
                            score = muscle.score,
                            currentRank = muscle.rank,
                            highlight = muscle.isRankIncreased
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MuscleProgressionDialogPreview() {
    WorkoutedTheme {
        MuscleProgressionDialog(
            progression = MuscleGroupProgression(
                id = "group_chest",
                score = 125.0,
                rank = Rank.SILVER,
                muscles = listOf(
                    MuscleProgression("muscle_chest_upper", 80.0, Rank.BRONZE),
                    MuscleProgression("muscle_chest_mid", 150.0, Rank.SILVER)
                )
            ),
            localizedGroupName = "Chest",
            onDismiss = {}
        )
    }
}

@Composable
fun ProgressionScale(
    score: Double,
    currentRank: Rank,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val ranks = Rank.entries.sortedBy { it.minScore }
    val nextRank = ranks.getOrNull(ranks.indexOf(currentRank) + 1)
    
    var isHighlighted by remember { mutableStateOf(highlight) }
    
    LaunchedEffect(highlight) {
        if (highlight) {
            delay(1000) // Initial delay to show normal state
            isHighlighted = true
            delay(2000) // Highlight duration
            isHighlighted = false
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted) currentRank.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(1000),
        label = "highlightBackground"
    )

    val progress = if (nextRank != null) {
        val range = nextRank.minScore - currentRank.minScore
        val currentProgress = score - currentRank.minScore
        (currentProgress.toFloat() / range.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(currentRank.nameRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = currentRank.color
            )
            if (nextRank != null) {
                Text(
                    text = stringResource(nextRank.nameRes).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = nextRank.color.copy(alpha = 0.5f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            color = currentRank.color,
            trackColor = Color.Transparent,
            gapSize = 0.dp,
            drawStopIndicator = {},
            strokeCap = StrokeCap.Butt
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${score.toInt()} / ${nextRank?.minScore ?: "MAX"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
