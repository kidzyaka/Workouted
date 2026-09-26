package com.kidz.workouted.presentation.dashboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import com.kidz.workouted.R
import com.kidz.workouted.domain.model.Rank
import kotlinx.coroutines.delay

@Composable
fun RankUpOverlay(
    groupName: String,
    newRank: Rank,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }
    var isFilled by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    LaunchedEffect(isExiting) {
        if (isExiting) {
            startAnimation = false
            delay(600)
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
        
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        ) {
            val intensity = (this.value * 255).toInt().coerceIn(1, 255)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(80, intensity))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
            } catch (_: Exception) {}
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(400)
            }
        } catch (_: Exception) {}
        
        isFilled = true
    }

    AnimatedVisibility(
        visible = startAnimation,
        enter = fadeIn(animationSpec = tween(800, easing = EaseInCubic)) + 
                scaleIn(animationSpec = tween(800, easing = EaseOutBack), initialScale = 0.9f),
        exit = fadeOut(animationSpec = tween(600, easing = EaseOutCubic)) + 
               scaleOut(animationSpec = tween(600, easing = EaseInBack), targetScale = 1.1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(32.dp)
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            // Radiant Rays
            if (isFilled) {
                val infiniteTransition = rememberInfiniteTransition(label = "rays")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(10000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    newRank.color.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    newRank.color.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    newRank.color.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.rank_up_title).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = groupName.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = newRank.color,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Geometric Badge
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                    val pulseAlpha by rememberInfiniteTransition().animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(newRank.color.copy(alpha = pulseAlpha), Color.Transparent)))
                    )
                    
                    // Core Geometry based on rank power (approximated by color hash/length or just visual variety)
                    val rotationModifier = if (newRank.name.contains("DIAMOND") || newRank.name.contains("EMERALD") || newRank.name.contains("PLATINUM")) {
                        Modifier.graphicsLayer { rotationZ = 45f }
                    } else {
                        Modifier
                    }
                    
                    val shape = if (newRank.name.contains("ELITE") || newRank.name.contains("GOLD")) {
                        androidx.compose.foundation.shape.CutCornerShape(24.dp)
                    } else {
                        androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .then(rotationModifier)
                            .clip(shape)
                            .background(newRank.color)
                            .border(4.dp, Color.White.copy(alpha = 0.5f), shape)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = stringResource(newRank.nameRes).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = newRank.color
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = newRank.color,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                    strokeCap = StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AnimatedVisibility(visible = isFilled) {
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f)

                    Surface(
                        shape = CircleShape,
                        color = newRank.color,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current
                            ) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                isExiting = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "CLAIM RANK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
