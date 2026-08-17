package com.ali.rates.composeapp.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// AMOLED black + trading-green accent (mirrors the Flutter app)
private val Green = Color(0xFF00E5A0)
private val Cyan = Color(0xFF4DD0E1)

private val AmoledColors = darkColorScheme(
    primary = Green,
    onPrimary = Color(0xFF00201A),
    secondary = Cyan,
    onSecondary = Color(0xFF00232B),
    background = Color(0xFF000000),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFB3B3B3),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerLow = Color(0xFF0D0D0D),
    surfaceContainerHigh = Color(0xFF1C1C1C),
    surfaceContainerHighest = Color(0xFF262626),
    error = Color(0xFFFF5449),
)

val RatesShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun RatesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AmoledColors,
        shapes = RatesShapes,
        content = content,
    )
}
