package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode {
    LIGHT, AMOLED_DARK
}

private val AmoledDarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = AmoledBackground,
    primaryContainer = AmoledPillBadgeBg,
    onPrimaryContainer = PurpleAccent,
    secondary = PurpleSecondary,
    onSecondary = AmoledBackground,
    secondaryContainer = AmoledKeyOperatorBg,
    onSecondaryContainer = PurpleAccent,
    tertiary = PurpleTertiary,
    onTertiary = TextPrimary,
    background = AmoledBackground,
    onBackground = TextPrimary,
    surface = AmoledDisplaySurface,
    onSurface = TextPrimary,
    surfaceVariant = AmoledKeyOperatorBg,
    onSurfaceVariant = TextSecondary,
    outline = BlackBorder,
    outlineVariant = AmoledPillBadgeBorder
)

private val Puny4LightColorScheme = lightColorScheme(
    primary = LightKeyEqualBg,
    onPrimary = LightDisplaySurface,
    primaryContainer = LightPillBadgeBg,
    onPrimaryContainer = LightKeyOperatorText,
    secondary = LightKeyOperatorText,
    onSecondary = LightDisplaySurface,
    secondaryContainer = LightKeyOperatorBg,
    onSecondaryContainer = LightKeyOperatorText,
    tertiary = PurpleTertiary,
    onTertiary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightDisplaySurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightKeyOperatorBg,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightPillBadgeBorder
)

@Composable
fun Puny4Theme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val isDark = themeMode == AppThemeMode.AMOLED_DARK
    val colorScheme = if (isDark) AmoledDarkColorScheme else Puny4LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    val statusBarColor = if (isDark) AmoledBackground.toArgb() else LightBackground.toArgb()
                    window.statusBarColor = statusBarColor
                    window.navigationBarColor = statusBarColor
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = !isDark
                    insetsController.isAppearanceLightNavigationBars = !isDark
                }
            } catch (_: Throwable) {
                // Ignore any OEM window manager exception on custom ROMs
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
