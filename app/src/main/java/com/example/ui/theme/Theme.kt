package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BlueLight,
    onPrimary = NavyDark,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = Color.White,
    secondary = GreenLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = GreenBackground,
    tertiary = AmberWarning,
    onTertiary = Color.Black,
    error = RedLight,
    onError = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = BlueSurface,
    onPrimaryContainer = NavyPrimary,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = GreenBackground,
    onSecondaryContainer = GreenAccent,
    tertiary = GreenAccent,
    onTertiary = Color.White,
    error = RedDanger,
    onError = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE8EEFA),
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
