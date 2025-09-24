package com.reznor.emulation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Game Console Theme Colors
private val ConsolePrimary = Color(0xFF1A1A1A)
private val ConsoleSecondary = Color(0xFF2A2A2A)
private val ConsoleTertiary = Color(0xFF3A3A3A)
private val ConsoleAccent = Color(0xFF00D4FF)
private val ConsoleAccentSecondary = Color(0xFF00A8CC)
private val ConsoleText = Color(0xFFFFFFFF)
private val ConsoleTextSecondary = Color(0xFFB0B0B0)
private val ConsoleError = Color(0xFFFF4444)

private val ConsoleColorScheme = darkColorScheme(
    primary = ConsoleAccent,
    onPrimary = ConsolePrimary,
    primaryContainer = ConsoleAccentSecondary,
    onPrimaryContainer = ConsoleText,

    secondary = ConsoleSecondary,
    onSecondary = ConsoleText,
    secondaryContainer = ConsoleTertiary,
    onSecondaryContainer = ConsoleText,

    tertiary = ConsoleTertiary,
    onTertiary = ConsoleText,
    tertiaryContainer = ConsoleSecondary,
    onTertiaryContainer = ConsoleText,

    error = ConsoleError,
    onError = ConsoleText,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = ConsoleText,

    background = ConsolePrimary,
    onBackground = ConsoleText,
    surface = ConsoleSecondary,
    onSurface = ConsoleText,
    surfaceVariant = ConsoleTertiary,
    onSurfaceVariant = ConsoleTextSecondary,

    outline = ConsoleTextSecondary,
    outlineVariant = Color(0xFF404040),

    scrim = Color.Black.copy(alpha = 0.5f),
    surfaceTint = ConsoleAccent
)

@Composable
fun ReznorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ConsoleColorScheme,
        typography = Typography,
        content = content
    )
}