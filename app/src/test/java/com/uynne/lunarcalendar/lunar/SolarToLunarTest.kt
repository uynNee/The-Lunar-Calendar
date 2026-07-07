package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Test

class SolarToLunarTest {

    private fun assertLunar(
        solarDay: Int, solarMonth: Int, solarYear: Int,
        day: Int, month: Int, year: Int, leap: Boolean = false,
    ) {
        assertEquals(
            "$solarDay/$solarMonth/$solarYear",
            LunarDate(day, month, year, leap),
            LunarCalendar.solarToLunar(solarDay, solarMonth, solarYear),
        )
    }

    @Test
    fun `tet dates across years`() {
        assertLunar(31, 1, 1900, 1, 1, 1900)
        // UTC+7 discriminator: a UTC+8 calendar puts Tet 1968 on Jan 30.
        assertLunar(29, 1, 1968, 1, 1, 1968)
        assertLunar(5, 2, 2000, 1, 1, 2000)
        assertLunar(12, 2, 2021, 1, 1, 2021)
        assertLunar(1, 2, 2022, 1, 1, 2022)
        assertLunar(22, 1, 2023, 1, 1, 2023)
        assertLunar(10, 2, 2024, 1, 1, 2024)
        assertLunar(29, 1, 2025, 1, 1, 2025)
        assertLunar(17, 2, 2026, 1, 1, 2026)
    }

    @Test
    fun `historic dates`() {
        assertLunar(2, 9, 1945, 26, 7, 1945)
        assertLunar(30, 4, 1975, 20, 3, 1975)
    }

    @Test
    fun `ram thang gieng 2024`() {
        assertLunar(24, 2, 2024, 15, 1, 2024)
    }

    @Test
    fun `day before tet is end of previous lunar year`() {
        assertLunar(9, 2, 2024, 30, 12, 2023)
    }
}
