package com.uynne.lunarcalendar.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceModeTest {

    @Test
    fun fromStoredValueRecognizesKnownModes() {
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromStoredValue("system"))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromStoredValue("light"))
        assertEquals(AppearanceMode.DARK, AppearanceMode.fromStoredValue("dark"))
    }

    @Test
    fun fromStoredValueFallsBackToSystem() {
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromStoredValue(null))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromStoredValue("unknown"))
    }
}
