package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Base Cyber Dark Colors
val ShenBackground = Color(0xFF08080C)
val ShenSurface = Color(0xFF0F0F17)
val ShenSurfaceVariant = Color(0xFF171724)
val ShenSurfaceElevated = Color(0xFF1E1E30)

// Vibrant Cyberpunk RGB Spectrum
val ShenNeonPink = Color(0xFFFF3D81)
val ShenNeonPurple = Color(0xFF7B2FF7)
val ShenNeonCyan = Color(0xFF00E5FF)
val ShenNeonGreen = Color(0xFF00FF9D)

// Text & Accents
val ShenTextPrimary = Color(0xFFF4F6FB)
val ShenTextSecondary = Color(0xFF98A2B3)
val ShenTextTertiary = Color(0xFF667085)
val ShenBorderGlow = Color(0x3300E5FF)
val ShenBorderSubtle = Color(0x22FFFFFF)

// Gradients
val ShenRgbGradient = Brush.linearGradient(
    colors = listOf(ShenNeonPink, ShenNeonPurple, ShenNeonCyan)
)

val ShenCyanGlow = Brush.linearGradient(
    colors = listOf(ShenNeonCyan, ShenNeonPurple)
)

val ShenPinkPurpleGlow = Brush.linearGradient(
    colors = listOf(ShenNeonPink, ShenNeonPurple)
)

val ShenGlassSurface = Color(0x33141422)
val ShenGlassBorder = Color(0x3D7B2FF7)
