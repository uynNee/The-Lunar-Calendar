package com.uynne.lunarcalendar.widget

import com.uynne.lunarcalendar.calendar.buildMonthGrid
import com.uynne.lunarcalendar.lunar.LunarCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class WidgetSnapshotTest {

    @Test
    fun `next mung 1 from mid month`() {
        val from = LocalDate.of(2026, 7, 7) // 23/5 Bính Ngọ
        val next = nextLunarDay(from, 1)
        assertEquals(1, LunarCalendar.solarToLunar(next).day)
        assertTrue(next.isAfter(from))
        assertTrue(next.toEpochDay() - from.toEpochDay() <= 30)
    }

    @Test
    fun `next ram just after ram waits a full month`() {
        // Find a date that is the 16th lunar day, then next rằm must be 29 or 30 days out.
        var date = LocalDate.of(2026, 3, 1)
        while (LunarCalendar.solarToLunar(date).day != 16) date = date.plusDays(1)
        val next = nextLunarDay(date, 15)
        val gap = next.toEpochDay() - date.toEpochDay()
        assertEquals(15, LunarCalendar.solarToLunar(next).day)
        assertTrue("gap $gap", gap in 28..29)
    }

    @Test
    fun `today counts as zero`() {
        // Tết Bính Ngọ: 17/02/2026 is mùng 1.
        val tet = LocalDate.of(2026, 2, 17)
        val snapshot = buildWidgetSnapshot(tet)
        assertEquals(0, snapshot.daysToMung1)
        assertEquals(tet, snapshot.nextMung1)

        val ram = nextLunarDay(tet, 15)
        assertEquals(0, buildWidgetSnapshot(ram).daysToRam)
    }

    @Test
    fun `scan crosses month boundary before tet 2026`() {
        // Day before Tết = 29 or 30 tháng Chạp; next mùng 1 is the very next day.
        val eve = LocalDate.of(2026, 2, 16)
        val snapshot = buildWidgetSnapshot(eve)
        assertEquals(1, snapshot.daysToMung1)
        assertEquals(LocalDate.of(2026, 2, 17), snapshot.nextMung1)
    }

    @Test
    fun `scan works inside leap month 2025`() {
        // 1/6 nhuận 2025 = 25/7/2025; from inside the leap month scanning still finds day 15 there.
        val insideLeap = LocalDate.of(2025, 7, 26) // 2/6 nhuận
        val next = nextLunarDay(insideLeap, 15)
        val lunar = LunarCalendar.solarToLunar(next)
        assertEquals(15, lunar.day)
        assertEquals(6, lunar.month)
        assertTrue(lunar.isLeapMonth)
    }

    @Test
    fun `year end rollover`() {
        val snapshot = buildWidgetSnapshot(LocalDate.of(2026, 12, 31))
        assertTrue(snapshot.daysToMung1 in 0..30)
        assertTrue(snapshot.daysToRam in 0..30)
        assertEquals(1, LunarCalendar.solarToLunar(snapshot.nextMung1).day)
        assertEquals(15, LunarCalendar.solarToLunar(snapshot.nextRam).day)
    }

    @Test
    fun `snapshot coheres with engine`() {
        val today = LocalDate.of(2026, 7, 7)
        val snapshot = buildWidgetSnapshot(today)
        assertEquals(LunarCalendar.solarToLunar(today), snapshot.lunar)
        assertEquals(LunarCalendar.canChiOfDay(7, 7, 2026), snapshot.dayCanChi)
        assertEquals(LunarCalendar.canChiOfYear(snapshot.lunar.year), snapshot.yearCanChi)
        assertEquals(LunarCalendar.moonPhase(7, 7, 2026), snapshot.moonPhase)
        assertEquals(buildMonthGrid(YearMonth.of(2026, 7), today), snapshot.monthGrid)
    }

    @Test
    fun `countdowns bounded over a full year`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(365) {
            val snapshot = buildWidgetSnapshot(date)
            assertTrue("$date mung1 ${snapshot.daysToMung1}", snapshot.daysToMung1 in 0..30)
            assertTrue("$date ram ${snapshot.daysToRam}", snapshot.daysToRam in 0..30)
            date = date.plusDays(1)
        }
    }
}
