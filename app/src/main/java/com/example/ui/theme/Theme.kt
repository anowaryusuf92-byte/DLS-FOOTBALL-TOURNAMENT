package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TrophyGold,
    onPrimary = Color(0xFF1E1500),
    primaryContainer = TrophyGoldDark,
    onPrimaryContainer = Color(0xFFFFF3D6),
    secondary = StadiumCyan,
    onSecondary = Color(0xFF001F28),
    secondaryContainer = Color(0xFF003544),
    onSecondaryContainer = Color(0xFFCBE9FE),
    tertiary = GrassGreen,
    onTertiary = Color(0xFF00210E),
    background = PitchDark,
    onBackground = TextPrimary,
    surface = PitchSurface,
    onSurface = TextPrimary,
    surfaceVariant = PitchSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = LiveRed
)

private val LightColorScheme = DarkColorScheme // Football / DLS gaming tournament apps look premier in dark stadium theme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
