package af.namazia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import af.namazia.app.R

/**
 * Vazirmatn — a Perso-Arabic family bundled with the app (SIL OFL 1.1, see licenses/).
 * Bundling matters: OEM system fonts render Dari inconsistently and some drop glyphs
 * entirely, so relying on the device font makes the UI unpredictable in our market.
 */
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_light, FontWeight.Light),
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/**
 * Perso-Arabic script sits taller than Latin and carries descenders that the Material
 * defaults clip, so every style here gets a deliberately generous lineHeight (~1.6×)
 * and letterSpacing is kept at 0 — tracking breaks Arabic letter joining.
 */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Light,
        fontSize = 52.sp, lineHeight = 64.sp, letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Light,
        fontSize = 42.sp, lineHeight = 54.sp, letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 40.sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 36.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 30.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 30.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium,
        fontSize = 17.sp, lineHeight = 27.sp, letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 27.sp, letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 20.sp, letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.sp
    )
)

/** Quranic/du'a Arabic needs more room still, and reads best a size up. */
val ArabicVerse = TextStyle(
    fontFamily = Vazirmatn,
    fontWeight = FontWeight.Medium,
    fontSize = 19.sp,
    lineHeight = 38.sp,
    letterSpacing = 0.sp
)
