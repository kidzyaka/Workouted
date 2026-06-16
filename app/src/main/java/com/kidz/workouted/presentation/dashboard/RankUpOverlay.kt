package com.kidz.workouted.presentation.dashboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val progress = remember { Animatable(0f) }
    
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
        
        // Progress animation from 0 to 1 with haptic feedback
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
        ) {
            // Increasing vibration intensity
            val intensity = (this.value * 255).toInt().coerceIn(1, 255)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(80, intensity))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
            } catch (_: Exception) {
                // Ignore vibration errors
            }
        }
        
        // Final strong vibration
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(400)
            }
        } catch (_: Exception) {
            // Ignore vibration errors
        }
        
        delay(1200)
        startAnimation = false // Trigger exit animation
        delay(600) // Wait for exit animation to finish
        onFinished()
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
                .pointerInput(Unit) {}, // Consume all touch events
            contentAlignment = Alignment.Center
        ) {
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
                
                // Rank Badge with Glow
                Box(contentAlignment = Alignment.Center) {
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
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(newRank.color.copy(alpha = pulseAlpha), Color.Transparent)
                                )
                            )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(newRank.color)
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
                
                // Filling scale
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
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}
