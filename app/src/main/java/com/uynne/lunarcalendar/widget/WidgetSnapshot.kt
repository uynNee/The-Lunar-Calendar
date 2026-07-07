package com.uynne.lunarcalendar.widget

import com.uynne.lunarcalendar.calendar.MonthGrid
import com.uynne.lunarcalendar.calendar.buildMonthGrid
import com.uynne.lunarcalendar.data.Holiday
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.lunar.CanChi
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.lunar.LunarDate
import com.uynne.lunarcalendar.lunar.MoonPhase
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Everything the widgets render, computed once per refresh. Pure Kotlin. */
data class WidgetSnapshot(
    val today: LocalDate,
    val lunar: LunarDate,
    val dayCanChi: CanChi,
    val monthCanChi: CanChi,
    val yearCanChi: CanChi,
    val moonPhase: MoonPhase,
    val dayQuality: DayQuality,
    val nextMung1: LocalDate,
    val daysToMung1: Int,
    val nextRam: LocalDate,
    val daysToRam: Int,
    val monthGrid: MonthGrid,
    val holidays: List<Holiday>,
)

fun buildWidgetSnapshot(today: LocalDate = LocalDate.now()): WidgetSnapshot {
    val lunar = LunarCalendar.solarToLunar(today)
    val nextMung1 = nextLunarDay(today, 1)
    val nextRam = nextLunarDay(today, 15)
    return WidgetSnapshot(
        today = today,
        lunar = lunar,
        dayCanChi = LunarCalendar.canChiOfDay(today.dayOfMonth, today.monthValue, today.year),
        monthCanChi = LunarCalendar.canChiOfMonth(lunar.month, lunar.year),
        yearCanChi = LunarCalendar.canChiOfYear(lunar.year),
        moonPhase = LunarCalendar.moonPhase(today.dayOfMonth, today.monthValue, today.year),
        dayQuality = LunarCalendar.dayQuality(today.dayOfMonth, today.monthValue, today.year),
        nextMung1 = nextMung1,
        daysToMung1 = ChronoUnit.DAYS.between(today, nextMung1).toInt(),
        nextRam = nextRam,
        daysToRam = ChronoUnit.DAYS.between(today, nextRam).toInt(),
        monthGrid = buildMonthGrid(YearMonth.from(today), today),
        holidays = Holidays.on(today),
    )
}

/**
 * First date >= [from] whose lunar day equals [targetDay]. A lunar month is at
 * most 30 days, so the scan is bounded to 31 iterations.
 */
internal fun nextLunarDay(from: LocalDate, targetDay: Int): LocalDate {
    var date = from
    repeat(31) {
        if (LunarCalendar.solarToLunar(date).day == targetDay) return date
        date = date.plusDays(1)
    }
    error("No lunar day $targetDay within 31 days of $from")
}
