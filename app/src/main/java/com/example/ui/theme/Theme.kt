package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeoTealPrimary,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = NeoTealDark,
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = NeoAmberAccent,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = NeoAmberDark,
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = NeoTealLight,
    onTertiary = Color(0xFF042F2E),
    background = NeoDarkBackground,
    onBackground = NeoDarkTextPrimary,
    surface = NeoDarkSurface,
    onSurface = NeoDarkTextPrimary,
    surfaceVariant = NeoDarkSurfaceElevated,
    onSurfaceVariant = NeoDarkTextSecondary,
    outline = NeoDarkBorder,
    error = NeoError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeoTealDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = NeoAmberDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = NeoTealPrimary,
    onTertiary = Color.White,
    background = NeoLightBackground,
    onBackground = NeoLightTextPrimary,
    surface = NeoLightSurface,
    onSurface = NeoLightTextPrimary,
    surfaceVariant = NeoLightSurfaceElevated,
    onSurfaceVariant = NeoLightTextSecondary,
    outline = NeoLightBorder,
    error = NeoError,
    onError = Color.White
)

@Composable
fun NeoTheme(
    darkTheme: Boolean = true, // Default to sleek touring dark theme as preferred by prompt
    dynamicColor: Boolean = false, // Keep distinct NEO brand identity
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NeoTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
