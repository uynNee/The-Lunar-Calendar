package com.uynne.lunarcalendar.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF4F6354),
    secondary = Color(0xFF586249),
    tertiary = Color(0xFF3D6372),
    background = Color(0xFFFCFCF7),
    surface = Color(0xFFFCFCF7),
    onBackground = Color(0xFF1A1C19),
    onSurfaceVariant = Color(0xFF44483F)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFB7CCB9),
    secondary = Color(0xFFC0CAAD),
    tertiary = Color(0xFFA5CDDC),
    background = Color(0xFF11140F),
    surface = Color(0xFF11140F),
    onBackground = Color(0xFFE2E3DD),
    onSurfaceVariant = Color(0xFFC4C8BD)
)

@Immutable
data class ExtendedColors(
    val holidayRed: Color,
    val hoangDao: Color,
    val hacDao: Color,
)

val LightExtendedColors = ExtendedColors(
    holidayRed = Color(0xFFB3261E),
    hoangDao = Color(0xFF3A6B35),
    hacDao = Color(0xFF7A5A00),
)

val DarkExtendedColors = ExtendedColors(
    holidayRed = Color(0xFFFFB4AB),
    hoangDao = Color(0xFF9CD67D),
    hacDao = Color(0xFFE0C36C),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
