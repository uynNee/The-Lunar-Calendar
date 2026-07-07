package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeapMonthTest {

    @Test
    fun `2023 leap month is 2`() {
        assertEquals(2, LunarCalendar.leapMonthOf(2023))
        assertEquals(LunarDate(30, 2, 2023, false), LunarCalendar.solarToLunar(21, 3, 2023))
        assertEquals(LunarDate(1, 2, 2023, true), LunarCalendar.solarToLunar(22, 3, 2023))
        assertEquals(LunarDate(1, 3, 2023, false), LunarCalendar.solarToLunar(20, 4, 2023))
    }

    @Test
    fun `2025 leap month is 6`() {
        assertEquals(6, LunarCalendar.leapMonthOf(2025))
        assertEquals(LunarDate(1, 6, 2025, true), LunarCalendar.solarToLunar(25, 7, 2025))
    }

    @Test
    fun `leap map 2001-2028`() {
        val expected = mapOf(
            2001 to 4, 2004 to 2, 2006 to 7, 2009 to 5, 2012 to 4, 2014 to 9,
            2017 to 6, 2020 to 4, 2023 to 2, 2025 to 6, 2028 to 5,
        )
        for (year in 2001..2028) {
            assertEquals("year $year", expected[year] ?: 0, LunarCalendar.leapMonthOf(year))
        }
    }

    @Test
    fun `metonic structure holds 1900-2100`() {
        val leapYears = (1900..2100).filter { LunarCalendar.leapMonthOf(it) != 0 }
        for (start in 1900..2082) {
            val count = leapYears.count { it in start until start + 19 }
            assertTrue("window $start has $count leaps", count in 6..8)
        }
        assertTrue("total leap years ${leapYears.size}", leapYears.size in 72..76)
    }

    @Test
    fun `invalid leap flag returns null`() {
        assertNull(LunarCalendar.lunarToSolar(1, 3, 2023, isLeapMonth = true)) // 2023's leap month is 2
        assertNull(LunarCalendar.lunarToSolar(1, 6, 2024, isLeapMonth = true)) // 2024 has no leap month
    }
}
