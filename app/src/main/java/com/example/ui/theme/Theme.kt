package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ShenColorScheme = darkColorScheme(
    primary = ShenNeonCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = ShenNeonCyan,
    secondary = ShenNeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF370077),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = ShenNeonPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF7A0033),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = ShenBackground,
    onBackground = ShenTextPrimary,
    surface = ShenSurface,
    onSurface = ShenTextPrimary,
    surfaceVariant = ShenSurfaceVariant,
    onSurfaceVariant = ShenTextSecondary,
    outline = ShenBorderSubtle,
    outlineVariant = ShenBorderGlow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force futuristic cyber dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShenColorScheme,
        typography = Typography,
        content = content
    )
}
