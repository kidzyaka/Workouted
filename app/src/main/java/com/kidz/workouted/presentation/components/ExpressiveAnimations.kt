package com.kidz.workouted.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun StaggeredEntranceItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Softened overshoot curve for a more premium, less aggressive feel
    val expressiveEasing = CubicBezierEasing(0.2f, 1.25f, 0.4f, 1.0f)
    
    LaunchedEffect(Unit) {
        val staggerDelay = when (index) {
            0 -> 0L
            1 -> 30L
            else -> 30L + (index - 1) * 20L
        }
        kotlinx.coroutines.delay(staggerDelay)
        startAnimation = true
    }

    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = expressiveEasing),
        label = "entranceProgress"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress
            // Subtler scale: from 0.99 to 1.0
            scaleX = 0.99f + (0.01f * progress)
            scaleY = 0.99f + (0.01f * progress)
            // Subtler jump: from 12dp to 0
            translationY = (12.dp.toPx()) * (1f - progress)
        }
    ) {
        content()
    }
}
