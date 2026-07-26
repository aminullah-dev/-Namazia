package com.kabulsignal.azanapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Lapis + gold: the palette of Afghan lapis lazuli and manuscript illumination.
val Lapis = Color(0xFF1A3A6B)
val LapisLight = Color(0xFF2D5FAA)
val Moonlight = Color(0xFFF5F2EB)
val GoldAccent = Color(0xFFBFA15C)
val GoldSoft = Color(0xFFD9C48A)

private val LightColorScheme = lightColorScheme(
    primary = Lapis,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE7FA),
    onPrimaryContainer = Color(0xFF0A1F45),
    secondary = LapisLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EBF8),
    onSecondaryContainer = Color(0xFF12294F),
    tertiary = Color(0xFF8A7434),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6EDD6),
    onTertiaryContainer = Color(0xFF3D3312),
    background = Moonlight,
    onBackground = Color(0xFF1B1B20),
    surface = Color.White,
    onSurface = Color(0xFF1B1B20),
    surfaceVariant = Color(0xFFE8E3D9),
    onSurfaceVariant = Color(0xFF4A463E),
    surfaceTint = Lapis,
    outline = Color(0xFF7C786F),
    outlineVariant = Color(0xFFD2CCC0),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9EBEFF),
    onPrimary = Color(0xFF00305F),
    primaryContainer = Color(0xFF1B4585),
    onPrimaryContainer = Color(0xFFD9E4FF),
    secondary = Color(0xFFAFC7F5),
    onSecondary = Color(0xFF152F5A),
    secondaryContainer = Color(0xFF264674),
    onSecondaryContainer = Color(0xFFDBE6FF),
    tertiary = GoldSoft,
    onTertiary = Color(0xFF3A2F0B),
    tertiaryContainer = Color(0xFF564519),
    onTertiaryContainer = Color(0xFFF6E7BE),
    background = Color(0xFF11151E),
    onBackground = Color(0xFFE4E2DC),
    surface = Color(0xFF171D29),
    onSurface = Color(0xFFE4E2DC),
    surfaceVariant = Color(0xFF262E3D),
    onSurfaceVariant = Color(0xFFC6C2B9),
    surfaceTint = Color(0xFF9EBEFF),
    outline = Color(0xFF8C8A83),
    outlineVariant = Color(0xFF3A4150),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun AzanAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Match the bars to the top-bar / nav-bar surface so the chrome reads as one piece.
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
