package com.uynne.lunarcalendar.calendar

import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.lunar.LunarDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MonthGridTest {

    @Test
    fun `july 2026 grid shape`() {
        val today = LocalDate.of(2026, 7, 7)
        val grid = buildMonthGrid(YearMonth.of(2026, 7), today)
        assertEquals(6, grid.weeks.size)
        assertTrue(grid.weeks.all { it.size == 7 })
        // 1/7/2026 is a Wednesday; Monday-first grid starts 29/6/2026.
        assertEquals(LocalDate.of(2026, 6, 29), grid.weeks.first().first().date)
        assertEquals(31, grid.weeks.flatten().count { it.inCurrentMonth })
        assertEquals(1, grid.weeks.flatten().count { it.isToday })
        assertTrue(grid.weeks.flatten().single { it.isToday }.date == today)
    }

    @Test
    fun `cells carry engine lunar dates`() {
        val grid = buildMonthGrid(YearMonth.of(2025, 1), LocalDate.of(2025, 1, 1))
        for (cell in grid.weeks.flatten()) {
            assertEquals(LunarCalendar.solarToLunar(cell.date), cell.lunar)
        }
    }

    @Test
    fun `tet cell in january 2025 has holidays and highlight flags`() {
        val grid = buildMonthGrid(YearMonth.of(2025, 1), LocalDate.of(2025, 1, 1))
        val tet = grid.weeks.flatten().single { it.date == LocalDate.of(2025, 1, 29) }
        assertTrue(tet.isLunarFirst)
        assertTrue(tet.holidays.isNotEmpty())
    }

    @Test
    fun `lunar day labels`() {
        assertEquals("1/2", lunarDayLabel(LunarDate(1, 2, 2023, false)))
        assertEquals("1/2N", lunarDayLabel(LunarDate(1, 2, 2023, true)))
        assertEquals("15", lunarDayLabel(LunarDate(15, 8, 2024, false)))
        assertEquals("29", lunarDayLabel(LunarDate(29, 12, 2024, false)))
    }

    @Test
    fun `leap month start cell shows N notation`() {
        // 1/6 nhuận 2025 = 25/7/2025.
        val solar = LunarCalendar.lunarToSolar(1, 6, 2025, isLeapMonth = true)!!.toLocalDate()
        val grid = buildMonthGrid(YearMonth.from(solar), solar)
        val cell = grid.weeks.flatten().single { it.date == solar }
        assertEquals("1/6N", lunarDayLabel(cell.lunar))
    }
}
