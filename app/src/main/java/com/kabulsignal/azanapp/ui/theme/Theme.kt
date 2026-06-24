package com.kabulsignal.azanapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Lapis = Color(0xFF1A3A6B)
val LapisLight = Color(0xFF2D5FAA)
val Moonlight = Color(0xFFF0EDE6)
val GoldAccent = Color(0xFFBFA15C)

private val LightColorScheme = lightColorScheme(
    primary = Lapis,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001C4A),
    secondary = LapisLight,
    onSecondary = Color.White,
    tertiary = GoldAccent,
    onTertiary = Color.White,
    background = Moonlight,
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8E4DC),
    onSurfaceVariant = Color(0xFF44403C),
    outline = Color(0xFF9E9A94),
    error = Color(0xFFBA1A1A),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FB4FF),
    onPrimary = Color(0xFF002F73),
    primaryContainer = Color(0xFF0A3A8C),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF9EBEFF),
    tertiary = GoldAccent,
    background = Color(0xFF0F1729),
    onBackground = Color(0xFFE4E2DC),
    surface = Color(0xFF1A2540),
    onSurface = Color(0xFFE4E2DC),
    surfaceVariant = Color(0xFF242F47),
    onSurfaceVariant = Color(0xFFC4C0B8),
)

@Composable
fun AzanAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
