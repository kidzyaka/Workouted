package com.kidz.workouted.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.graphics.ColorUtils
import androidx.compose.material3.ColorScheme

import com.kidz.workouted.domain.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val AmoledColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = androidx.compose.ui.graphics.Color.Black,
    onBackground = OnBackgroundDark,
    surface = androidx.compose.ui.graphics.Color.Black,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

private fun applyCustomAccent(
    baseScheme: ColorScheme,
    customColor: androidx.compose.ui.graphics.Color,
    isDark: Boolean
): ColorScheme {
    val baseInt = customColor.toArgb()
    val bgInt = if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    
    // Primary Container is 70% background color, 30% primary color
    val primaryContainerInt = ColorUtils.blendARGB(baseInt, bgInt, 0.7f)
    
    // Secondary is 40% saturation of primary
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(baseInt, hsl)
    val secondaryInt = ColorUtils.HSLToColor(floatArrayOf(hsl[0], hsl[1] * 0.4f, hsl[2]))
    
    // Secondary Container is 70% background color, 30% secondary color
    val secondaryContainerInt = ColorUtils.blendARGB(secondaryInt, bgInt, 0.7f)
    
    // Determine text colors based on luminance for contrast
    val onPrimary = if (ColorUtils.calculateLuminance(baseInt) > 0.5) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
    val onPrimaryContainer = if (ColorUtils.calculateLuminance(primaryContainerInt) > 0.5) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
    
    val onSecondary = if (ColorUtils.calculateLuminance(secondaryInt) > 0.5) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
    val onSecondaryContainer = if (ColorUtils.calculateLuminance(secondaryContainerInt) > 0.5) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White

    return baseScheme.copy(
        primary = customColor,
        onPrimary = onPrimary,
        primaryContainer = androidx.compose.ui.graphics.Color(primaryContainerInt),
        onPrimaryContainer = onPrimaryContainer,
        
        secondary = androidx.compose.ui.graphics.Color(secondaryInt),
        onSecondary = onSecondary,
        secondaryContainer = androidx.compose.ui.graphics.Color(secondaryContainerInt),
        onSecondaryContainer = onSecondaryContainer
    )
}

@Composable
fun WorkoutedTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    userColor: String? = null,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK, AppTheme.AMOLED -> true
        AppTheme.SYSTEM -> isSystemDark
    }
    
    var colorScheme = when (theme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
        AppTheme.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
    }
    
    if (userColor != null) {
        try {
            val customColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(userColor))
            colorScheme = applyCustomAccent(colorScheme, customColor, isDark)
        } catch (e: Exception) {
            // Ignore parse errors, fallback to default theme primary color
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}