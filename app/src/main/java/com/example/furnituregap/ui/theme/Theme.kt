package com.example.furnituregap.ui.theme

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
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF003258),
    primaryContainer = BlueprintPrimary,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003355),
    secondaryContainer = BlueprintSecondary,
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = Color(0xFF80DEEA),
    onTertiary = Color(0xFF00363D),
    background = BlueprintCanvasDark,
    onBackground = Color(0xFFE2E8F0),
    surface = BlueprintSurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = BlueprintCardDark,
    onSurfaceVariant = Color(0xFFBAC7D5),
    outline = Color(0xFF455A64)
)

private val LightColorScheme = lightColorScheme(
    primary = BlueprintPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = BlueprintTertiary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7E7FF),
    onSecondaryContainer = Color(0xFF001E2E),
    tertiary = DraftingSlate,
    onTertiary = Color.White,
    background = BlueprintCanvasLight,
    onBackground = Color(0xFF1A202C),
    surface = BlueprintSurfaceLight,
    onSurface = Color(0xFF1A202C),
    surfaceVariant = BlueprintCardLight,
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFFCBD5E0)
)

@Composable
fun FurnitureGapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Architectural styling by default
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
