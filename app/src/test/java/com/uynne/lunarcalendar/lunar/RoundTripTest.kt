package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoundTripTest {

    @Test
    fun `every day 1900-2100 round trips and lunar structure holds`() {
        var date = LocalDate.of(1900, 1, 1)
        val end = LocalDate.of(2100, 12, 31)
        var prev: LunarDate? = null
        var monthLength = 0
        while (!date.isAfter(end)) {
            val lunar = LunarCalendar.solarToLunar(date)
            assertTrue("$date lunar day ${lunar.day}", lunar.day in 1..30)
            assertTrue("$date lunar month ${lunar.month}", lunar.month in 1..12)

            val solar = LunarCalendar.lunarToSolar(lunar.day, lunar.month, lunar.year, lunar.isLeapMonth)
            assertEquals("$date", SolarDate(date.dayOfMonth, date.monthValue, date.year), solar)

            if (prev == null) {
                monthLength = lunar.day // seed mid-month at range start
            } else if (lunar.day == 1) {
                assertTrue("$date month length $monthLength", monthLength == 29 || monthLength == 30)
                monthLength = 1
            } else {
                assertEquals("$date lunar day increments", prev.day + 1, lunar.day)
                monthLength++
            }
            prev = lunar
            date = date.plusDays(1)
        }
    }
}
