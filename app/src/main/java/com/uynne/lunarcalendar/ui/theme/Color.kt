package com.uynne.lunarcalendar.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    onPrimaryContainer = Color(0xFF00274D),
    secondary = Color(0xFF34C759),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCF5E3),
    onSecondaryContainer = Color(0xFF0F3D1D),
    tertiary = Color(0xFF5856D6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3E2FA),
    onTertiaryContainer = Color(0xFF1F1E5C),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF111113),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111113),
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF6E6E73),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF2F2F7),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF2F2F7),
    surfaceDim = Color(0xFFE5E5EA),
    surfaceBright = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFF2C2C2E),
    inverseOnSurface = Color(0xFFF5F5F7),
    inversePrimary = Color(0xFF6CAEFF),
    outline = Color(0xFFD1D1D6),
    outlineVariant = Color(0xFFE5E5EA),
    scrim = Color(0xFF000000),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFE1DE),
    onErrorContainer = Color(0xFF410E0B),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00385C),
    onPrimaryContainer = Color(0xFFD6E8FF),
    secondary = Color(0xFF30D158),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0F3D1D),
    onSecondaryContainer = Color(0xFFDCF5E3),
    tertiary = Color(0xFF5E5CE6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2E2C7A),
    onTertiaryContainer = Color(0xFFE3E2FA),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF2C2C2E),
    surfaceContainerHigh = Color(0xFF3A3A3C),
    surfaceContainerHighest = Color(0xFF48484A),
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF3A3A3C),
    inverseSurface = Color(0xFFF5F5F7),
    inverseOnSurface = Color(0xFF2C2C2E),
    inversePrimary = Color(0xFF007AFF),
    outline = Color(0xFF48484A),
    outlineVariant = Color(0xFF38383A),
    scrim = Color(0xFF000000),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFE1DE),
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
