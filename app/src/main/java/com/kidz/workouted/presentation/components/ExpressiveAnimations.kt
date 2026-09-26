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
    
    LaunchedEffect(Unit) {
        val staggerDelay = when (index) {
            0 -> 0L
            1 -> 30L
            else -> 30L + (index - 1) * 20L
        }
        kotlinx.coroutines.delay(staggerDelay)
        startAnimation = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "entranceAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.94f,
        animationSpec = spring(
            dampingRatio = 0.65f, // Deliberate expressive overshoot
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "entranceScale"
    )

    val translationY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "entranceTranslationY"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
            this.translationY = translationY.toPx()
        }
    ) {
        content()
    }
}
