package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Test

class LunarToSolarTest {

    @Test
    fun `tet inverses`() {
        assertEquals(SolarDate(31, 1, 1900), LunarCalendar.lunarToSolar(1, 1, 1900))
        assertEquals(SolarDate(29, 1, 1968), LunarCalendar.lunarToSolar(1, 1, 1968))
        assertEquals(SolarDate(5, 2, 2000), LunarCalendar.lunarToSolar(1, 1, 2000))
        assertEquals(SolarDate(10, 2, 2024), LunarCalendar.lunarToSolar(1, 1, 2024))
        assertEquals(SolarDate(29, 1, 2025), LunarCalendar.lunarToSolar(1, 1, 2025))
        assertEquals(SolarDate(17, 2, 2026), LunarCalendar.lunarToSolar(1, 1, 2026))
    }

    @Test
    fun `regular and leap month 2 of 2023 map to distinct solar dates`() {
        assertEquals(SolarDate(20, 2, 2023), LunarCalendar.lunarToSolar(1, 2, 2023, isLeapMonth = false))
        assertEquals(SolarDate(22, 3, 2023), LunarCalendar.lunarToSolar(1, 2, 2023, isLeapMonth = true))
    }

    @Test
    fun `historic inverses`() {
        assertEquals(SolarDate(2, 9, 1945), LunarCalendar.lunarToSolar(26, 7, 1945))
        assertEquals(SolarDate(30, 4, 1975), LunarCalendar.lunarToSolar(20, 3, 1975))
    }
}
