package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ImmersiveLavender,
    onPrimary = ImmersiveDarkPurple,
    primaryContainer = ImmersiveDeepPurple,
    onPrimaryContainer = ImmersiveLightPurple,
    secondary = ImmersiveLavender,
    onSecondary = ImmersiveDarkPurple,
    secondaryContainer = ImmersiveMediumPurple,
    onSecondaryContainer = ImmersiveLightPurple,
    tertiary = ImmersiveEmerald,
    onTertiary = Color.Black,
    background = ImmersiveBackground,
    onBackground = Color(0xFFF3F4F6),
    surface = ImmersiveSurface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0x33FFFFFF)
)

private val AmoledColorScheme = darkColorScheme(
    primary = ImmersiveLavender,
    onPrimary = Color.Black,
    primaryContainer = ImmersiveDeepPurple,
    onPrimaryContainer = ImmersiveLightPurple,
    secondary = ImmersiveLavender,
    onSecondary = Color.Black,
    secondaryContainer = ImmersiveMediumPurple,
    onSecondaryContainer = ImmersiveLightPurple,
    tertiary = ImmersiveEmerald,
    onTertiary = Color.Black,
    background = AmoledBackground,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = AmoledCard,
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = AmoledBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "Dark", // "Dark", "AMOLED", "Light", "System"
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        "AMOLED" -> AmoledColorScheme
        "Light" -> LightColorScheme
        "Dark" -> DarkColorScheme
        else -> if (isSystemDark) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val isLight = colorScheme.background == LightBackground
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
