package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MoonPhaseTest {

    @Test
    fun `lunar first days are new moon`() {
        for (month in 1..12) {
            val solar = LunarCalendar.lunarToSolar(1, month, 2024)!!
            assertEquals(
                "lunar 1/$month/2024",
                MoonPhase.NEW_MOON,
                LunarCalendar.moonPhase(solar.day, solar.month, solar.year),
            )
        }
    }

    @Test
    fun `ram is full moon`() {
        for (month in 1..12) {
            val solar = LunarCalendar.lunarToSolar(15, month, 2024)!!
            assertEquals(
                "lunar 15/$month/2024",
                MoonPhase.FULL_MOON,
                LunarCalendar.moonPhase(solar.day, solar.month, solar.year),
            )
        }
    }

    @Test
    fun `phases progress through all eight in one lunation`() {
        var date = LocalDate.of(2024, 2, 10) // 1/1 Giáp Thìn
        val seen = linkedSetOf<MoonPhase>()
        while (true) {
            val lunar = LunarCalendar.solarToLunar(date)
            if (lunar.month != 1 || lunar.year != 2024) break
            seen.add(LunarCalendar.moonPhase(date.dayOfMonth, date.monthValue, date.year))
            date = date.plusDays(1)
        }
        // Insertion order of first sightings must follow the phase cycle.
        assertEquals(MoonPhase.values().toList(), seen.toList())
    }
}
