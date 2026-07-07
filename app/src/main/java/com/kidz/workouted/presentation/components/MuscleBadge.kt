package com.kidz.workouted.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.kidz.workouted.domain.model.Rank

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue

@Composable
fun MuscleBadge(
    muscleName: String,
    rank: Rank,
    modifier: Modifier = Modifier,
    hasUnseenProgression: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            if (hasUnseenProgression) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(rank.color.copy(alpha = alpha * 0.4f))
                )
            }
            Surface(
                shape = CircleShape,
                color = rank.color,
                modifier = Modifier.size(14.dp),
                shadowElevation = if (hasUnseenProgression) 4.dp else 0.dp,
                border = if (hasUnseenProgression) BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null
            ) {}
        }
        
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
            modifier = Modifier.clip(MaterialTheme.shapes.extraSmall)
        ) {
            Text(
                text = muscleName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp
            )
        }
        
        Text(
            text = stringResource(rank.nameRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = rank.color,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
