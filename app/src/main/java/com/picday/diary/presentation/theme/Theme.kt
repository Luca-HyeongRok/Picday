package com.picday.diary.presentation.theme

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    background = AppColors.BackgroundDark,
    surface = AppColors.SurfaceDark,
    onBackground = AppColors.TextMainDark,
    onSurface = AppColors.TextMainDark,
    surfaceVariant = AppColors.SurfaceDark.copy(alpha = 0.5f),
    onSurfaceVariant = AppColors.TextSubDark
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    background = AppColors.BackgroundLight,
    surface = AppColors.SurfaceLight,
    onBackground = AppColors.TextMainLight,
    onSurface = AppColors.TextMainLight,
    surfaceVariant = AppColors.BackgroundLight,
    onSurfaceVariant = AppColors.TextSubLight
)

@Composable
fun PicDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to maintain our custom minimalist branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            var currentContext = context
            while (currentContext is ContextWrapper) {
                if (currentContext is Activity) break
                currentContext = currentContext.baseContext
            }
            val activity = currentContext as? Activity
            activity?.window?.let { window ->
                @Suppress("DEPRECATION")
                window.statusBarColor = ColorSchemeToStatusBarColor(colorScheme, darkTheme)
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun ColorSchemeToStatusBarColor(scheme: ColorScheme, isDark: Boolean): Int {
    // Return background color or primary color based on theme
    return scheme.background.toArgb()
}