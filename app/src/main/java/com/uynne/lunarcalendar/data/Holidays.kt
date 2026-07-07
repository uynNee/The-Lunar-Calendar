package com.uynne.lunarcalendar.data

import com.uynne.lunarcalendar.lunar.LunarCalendar
import java.time.LocalDate

enum class HolidayType { PUBLIC, TRADITIONAL }

sealed interface Holiday {
    val name: String
    val type: HolidayType
}

data class SolarHoliday(
    val day: Int,
    val month: Int,
    override val name: String,
    override val type: HolidayType,
) : Holiday

data class LunarHoliday(
    val day: Int,
    val month: Int,
    override val name: String,
    override val type: HolidayType,
) : Holiday

object Holidays {

    private val giaoThua = LunarHoliday(0, 0, "Giao thừa", HolidayType.PUBLIC)

    val all: List<Holiday> = listOf(
        giaoThua,
        LunarHoliday(1, 1, "Tết Nguyên Đán (Mùng 1)", HolidayType.PUBLIC),
        LunarHoliday(2, 1, "Mùng 2 Tết", HolidayType.PUBLIC),
        LunarHoliday(3, 1, "Mùng 3 Tết", HolidayType.PUBLIC),
        LunarHoliday(10, 3, "Giỗ Tổ Hùng Vương", HolidayType.PUBLIC),
        LunarHoliday(15, 1, "Rằm tháng Giêng", HolidayType.TRADITIONAL),
        LunarHoliday(3, 3, "Tết Hàn thực", HolidayType.TRADITIONAL),
        LunarHoliday(15, 4, "Lễ Phật Đản", HolidayType.TRADITIONAL),
        LunarHoliday(5, 5, "Tết Đoan Ngọ", HolidayType.TRADITIONAL),
        LunarHoliday(15, 7, "Lễ Vu Lan", HolidayType.TRADITIONAL),
        LunarHoliday(15, 8, "Tết Trung Thu", HolidayType.TRADITIONAL),
        LunarHoliday(23, 12, "Ông Công Ông Táo", HolidayType.TRADITIONAL),
        SolarHoliday(1, 1, "Tết Dương lịch", HolidayType.PUBLIC),
        SolarHoliday(30, 4, "Giải phóng miền Nam", HolidayType.PUBLIC),
        SolarHoliday(1, 5, "Quốc tế Lao động", HolidayType.PUBLIC),
        SolarHoliday(2, 9, "Quốc khánh", HolidayType.PUBLIC),
        SolarHoliday(14, 2, "Lễ Tình nhân", HolidayType.TRADITIONAL),
        SolarHoliday(8, 3, "Quốc tế Phụ nữ", HolidayType.TRADITIONAL),
        SolarHoliday(1, 6, "Quốc tế Thiếu nhi", HolidayType.TRADITIONAL),
        SolarHoliday(20, 10, "Phụ nữ Việt Nam", HolidayType.TRADITIONAL),
        SolarHoliday(20, 11, "Nhà giáo Việt Nam", HolidayType.TRADITIONAL),
        SolarHoliday(25, 12, "Giáng sinh", HolidayType.TRADITIONAL),
    )

    fun on(date: LocalDate): List<Holiday> {
        val lunar = LunarCalendar.solarToLunar(date)
        val result = mutableListOf<Holiday>()
        // Giao thừa: the last day of tháng Chạp, i.e. the day before a regular 1/1.
        val tomorrow = LunarCalendar.solarToLunar(date.plusDays(1))
        if (tomorrow.day == 1 && tomorrow.month == 1 && !tomorrow.isLeapMonth) {
            result += giaoThua
        }
        for (holiday in all) {
            when (holiday) {
                giaoThua -> Unit
                is SolarHoliday ->
                    if (holiday.day == date.dayOfMonth && holiday.month == date.monthValue) {
                        result += holiday
                    }
                is LunarHoliday ->
                    // Lunar holidays are observed in the regular month, never the leap month.
                    if (holiday.day == lunar.day && holiday.month == lunar.month && !lunar.isLeapMonth) {
                        result += holiday
                    }
            }
        }
        return result
    }
}
