package com.uynne.lunarcalendar.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetPreferenceTypesTest {

    @Test
    fun themeModeParsingRecognizesKnownValues() {
        assertEquals(WidgetThemeMode.SYSTEM, WidgetThemeMode.fromStoredValue("system"))
        assertEquals(WidgetThemeMode.LIGHT, WidgetThemeMode.fromStoredValue("light"))
        assertEquals(WidgetThemeMode.DARK, WidgetThemeMode.fromStoredValue("dark"))
        assertEquals(WidgetThemeMode.MATCH_APP, WidgetThemeMode.fromStoredValue("match_app"))
    }

    @Test
    fun themeModeParsingFallsBackToSystem() {
        assertEquals(WidgetThemeMode.SYSTEM, WidgetThemeMode.fromStoredValue(null))
        assertEquals(WidgetThemeMode.SYSTEM, WidgetThemeMode.fromStoredValue("unknown"))
    }

    @Test
    fun styleParsingRecognizesKnownValues() {
        assertEquals(WidgetStyle.MINIMAL, WidgetStyle.fromStoredValue("minimal"))
        assertEquals(WidgetStyle.CALENDAR, WidgetStyle.fromStoredValue("calendar"))
        assertEquals(WidgetStyle.LUNAR, WidgetStyle.fromStoredValue("lunar"))
        assertEquals(WidgetStyle.MOON, WidgetStyle.fromStoredValue("moon"))
        assertEquals(WidgetStyle.COMBINED, WidgetStyle.fromStoredValue("combined"))
    }

    @Test
    fun styleParsingFallsBackToCombined() {
        assertEquals(WidgetStyle.COMBINED, WidgetStyle.fromStoredValue(null))
        assertEquals(WidgetStyle.COMBINED, WidgetStyle.fromStoredValue("unknown"))
    }

    @Test
    fun accentParsingRecognizesKnownValues() {
        assertEquals(WidgetAccentColor.BLUE, WidgetAccentColor.fromStoredValue("blue"))
        assertEquals(WidgetAccentColor.PINK, WidgetAccentColor.fromStoredValue("pink"))
        assertEquals(WidgetAccentColor.GREEN, WidgetAccentColor.fromStoredValue("green"))
        assertEquals(WidgetAccentColor.WARM, WidgetAccentColor.fromStoredValue("warm"))
        assertEquals(WidgetAccentColor.MONOCHROME, WidgetAccentColor.fromStoredValue("monochrome"))
    }

    @Test
    fun accentParsingFallsBackToBlue() {
        assertEquals(WidgetAccentColor.BLUE, WidgetAccentColor.fromStoredValue(null))
        assertEquals(WidgetAccentColor.BLUE, WidgetAccentColor.fromStoredValue("unknown"))
    }
}
