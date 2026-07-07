package com.uynne.lunarcalendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F6354),
    secondary = Color(0xFF586249),
    tertiary = Color(0xFF3D6372),
    background = Color(0xFFFCFCF7),
    surface = Color(0xFFFCFCF7),
    onBackground = Color(0xFF1A1C19),
    onSurfaceVariant = Color(0xFF44483F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7CCB9),
    secondary = Color(0xFFC0CAAD),
    tertiary = Color(0xFFA5CDDC),
    background = Color(0xFF11140F),
    surface = Color(0xFF11140F),
    onBackground = Color(0xFFE2E3DD),
    onSurfaceVariant = Color(0xFFC4C8BD)
)

@Composable
fun LunarCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
