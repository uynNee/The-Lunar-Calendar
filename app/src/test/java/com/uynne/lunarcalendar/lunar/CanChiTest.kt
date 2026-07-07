package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CanChiTest {

    @Test
    fun `year can chi`() {
        assertEquals("Giáp Thìn", LunarCalendar.canChiOfYear(2024).display)
        assertEquals("Ất Tỵ", LunarCalendar.canChiOfYear(2025).display)
        assertEquals("Mậu Thân", LunarCalendar.canChiOfYear(1968).display)
        assertEquals("Ất Dậu", LunarCalendar.canChiOfYear(1945).display)
        assertEquals("Canh Thìn", LunarCalendar.canChiOfYear(2000).display)
    }

    @Test
    fun `thang gieng is always dan`() {
        for (year in 1900..2100) {
            assertEquals("year $year", Chi.DAN, LunarCalendar.canChiOfMonth(1, year).chi)
        }
        assertEquals("Bính Dần", LunarCalendar.canChiOfMonth(1, 2024).display)
        assertEquals("Canh Thìn", LunarCalendar.canChiOfMonth(3, 1975).display)
    }

    @Test
    fun `day can chi anchors`() {
        // Mùng 1 Tết Giáp Thìn was itself a Giáp Thìn day.
        assertEquals("Giáp Thìn", LunarCalendar.canChiOfDay(10, 2, 2024).display)
        assertEquals("Mậu Ngọ", LunarCalendar.canChiOfDay(1, 1, 2000).display)
        assertEquals("Bính Ngọ", LunarCalendar.canChiOfDay(30, 4, 1975).display)
    }

    @Test
    fun `sixty day cycle`() {
        var date = LocalDate.of(2024, 1, 1)
        repeat(20) {
            val a = LunarCalendar.canChiOfDay(date.dayOfMonth, date.monthValue, date.year)
            val later = date.plusDays(60)
            val b = LunarCalendar.canChiOfDay(later.dayOfMonth, later.monthValue, later.year)
            assertEquals("$date", a, b)
            date = date.plusDays(17)
        }
    }
}
