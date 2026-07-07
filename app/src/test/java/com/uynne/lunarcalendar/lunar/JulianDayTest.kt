package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class JulianDayTest {

    @Test
    fun `known anchor 2000-01-01 is jd 2451545`() {
        assertEquals(2451545, LunarCalendar.jdFromDate(1, 1, 2000))
    }

    @Test
    fun `matches java time epoch day over 1900-2100`() {
        var date = LocalDate.of(1900, 1, 1)
        val end = LocalDate.of(2100, 12, 31)
        while (!date.isAfter(end)) {
            val jd = LunarCalendar.jdFromDate(date.dayOfMonth, date.monthValue, date.year)
            assertEquals("$date", date.toEpochDay() + 2440588, jd.toLong())
            date = date.plusDays(1)
        }
    }

    @Test
    fun `jdToDate inverts jdFromDate across boundaries`() {
        val samples = listOf(
            Triple(28, 2, 1900), Triple(1, 3, 1900),
            Triple(29, 2, 2000), Triple(1, 3, 2000),
            Triple(28, 2, 2100), Triple(1, 3, 2100),
            Triple(31, 12, 1999), Triple(1, 1, 2000),
        )
        for ((d, m, y) in samples) {
            val jd = LunarCalendar.jdFromDate(d, m, y)
            assertEquals(SolarDate(d, m, y), LunarCalendar.jdToDate(jd))
        }
    }

    @Test
    fun `2100 is not a leap year`() {
        val jdFeb28 = LunarCalendar.jdFromDate(28, 2, 2100)
        assertEquals(SolarDate(1, 3, 2100), LunarCalendar.jdToDate(jdFeb28 + 1))
    }
}
