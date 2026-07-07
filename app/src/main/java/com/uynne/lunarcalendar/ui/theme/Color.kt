package com.uynne.lunarcalendar.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    secondary = Color(0xFF34C759),
    tertiary = Color(0xFF5856D6),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF111113),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111113),
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF6E6E73),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF2F2F7),
    outline = Color(0xFFD1D1D6),
    outlineVariant = Color(0xFFE5E5EA),
    error = Color(0xFFFF3B30),
    onError = Color.White,
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    secondary = Color(0xFF30D158),
    tertiary = Color(0xFF5E5CE6),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF2C2C2E),
    outline = Color(0xFF48484A),
    outlineVariant = Color(0xFF38383A),
    error = Color(0xFFFF453A),
    onError = Color.White,
)

@Immutable
data class ExtendedColors(
    val holidayRed: Color,
    val hoangDao: Color,
    val hacDao: Color,
    val separator: Color,
    val groupedBackground: Color,
    val selectedFill: Color,
    val selectedText: Color,
    val eventDot: Color,
    val moonAccent: Color,
)

val LightExtendedColors = ExtendedColors(
    holidayRed = Color(0xFFFF3B30),
    hoangDao = Color(0xFF248A3D),
    hacDao = Color(0xFFFF9500),
    separator = Color(0xFFE5E5EA),
    groupedBackground = Color(0xFFF5F5F7),
    selectedFill = Color(0xFF007AFF),
    selectedText = Color.White,
    eventDot = Color(0xFF007AFF),
    moonAccent = Color(0xFFFFCC00),
)

val DarkExtendedColors = ExtendedColors(
    holidayRed = Color(0xFFFF453A),
    hoangDao = Color(0xFF30D158),
    hacDao = Color(0xFFFF9F0A),
    separator = Color(0xFF38383A),
    groupedBackground = Color(0xFF000000),
    selectedFill = Color(0xFF0A84FF),
    selectedText = Color.White,
    eventDot = Color(0xFF0A84FF),
    moonAccent = Color(0xFFFFD60A),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
