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

/**
 * Material 3 Dark Color Scheme for NEO Biker Application.
 * Featuring Charcoal Background (`NeoCharcoalBackground`) and Electric Orange Accents (`NeoElectricOrange`).
 */
private val DarkColorScheme = darkColorScheme(
    primary = NeoElectricOrange,
    onPrimary = Color.White,
    primaryContainer = NeoElectricOrangeDark,
    onPrimaryContainer = Color(0xFFFFECE0),
    secondary = NeoElectricOrangeLight,
    onSecondary = Color(0xFF331400),
    secondaryContainer = Color(0xFF4A1F00),
    onSecondaryContainer = Color(0xFFFFDBC6),
    tertiary = NeoElectricOrangeLight,
    onTertiary = Color.White,
    background = NeoCharcoalBackground,
    onBackground = NeoDarkTextPrimary,
    surface = NeoCharcoalSurface,
    onSurface = NeoDarkTextPrimary,
    surfaceVariant = NeoCharcoalSurfaceElevated,
    onSurfaceVariant = NeoDarkTextSecondary,
    outline = NeoDarkBorder,
    error = NeoError,
    onError = Color.White
)

/**
 * Material 3 Light Color Scheme for NEO Biker Application.
 */
private val LightColorScheme = lightColorScheme(
    primary = NeoElectricOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE0),
    onPrimaryContainer = Color(0xFF662600),
    secondary = NeoElectricOrangeDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC6),
    onSecondaryContainer = Color(0xFF4A1F00),
    tertiary = NeoElectricOrange,
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
    darkTheme: Boolean = true, // Default to sleek touring dark charcoal theme
    dynamicColor: Boolean = false, // Preserve distinct NEO brand identity
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

