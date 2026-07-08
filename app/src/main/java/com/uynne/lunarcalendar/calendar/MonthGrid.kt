package com.uynne.lunarcalendar.calendar

import com.uynne.lunarcalendar.data.Holiday
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.lunar.LunarDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class DayCell(
    val date: LocalDate,
    val lunar: LunarDate,
    val inCurrentMonth: Boolean,
    val isToday: Boolean,
    val holidays: List<Holiday>,
) {
    val isLunarFirst: Boolean get() = lunar.day == 1
    val isRam: Boolean get() = lunar.day == 15
}

data class MonthGrid(
    val yearMonth: YearMonth,
    val weeks: List<List<DayCell>>,
)

/** Fixed 6x7 grid (starting on [weekStart]) including leading/trailing days of adjacent months. */
fun buildMonthGrid(yearMonth: YearMonth, today: LocalDate, weekStart: DayOfWeek = DayOfWeek.MONDAY): MonthGrid {
    val first = yearMonth.atDay(1)
    val offset = (first.dayOfWeek.value - weekStart.value + 7) % 7
    val start = first.minusDays(offset.toLong())
    val cells = (0 until 42).map { i ->
        val date = start.plusDays(i.toLong())
        DayCell(
            date = date,
            lunar = LunarCalendar.solarToLunar(date),
            inCurrentMonth = YearMonth.from(date) == yearMonth,
            isToday = date == today,
            holidays = Holidays.on(date),
        )
    }
    return MonthGrid(yearMonth, cells.chunked(7))
}

/** "1/M" on lunar month starts (with N suffix for leap months), plain day otherwise. */
fun lunarDayLabel(lunar: LunarDate): String =
    if (lunar.day == 1) {
        "1/${lunar.month}" + if (lunar.isLeapMonth) "N" else ""
    } else {
        "${lunar.day}"
    }
