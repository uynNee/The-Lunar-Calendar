package com.uynne.lunarcalendar.data

import com.uynne.lunarcalendar.lunar.LunarCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HolidaysTest {

    private fun names(date: LocalDate) = Holidays.on(date).map { it.name }

    @Test
    fun `gio to hung vuong 2024`() {
        // 10/3 âm lịch 2024 = 18/4/2024 dương lịch.
        assertEquals(
            LocalDate.of(2024, 4, 18),
            LunarCalendar.lunarToSolar(10, 3, 2024)!!.toLocalDate(),
        )
        assertTrue(names(LocalDate.of(2024, 4, 18)).contains("Giỗ Tổ Hùng Vương"))
    }

    @Test
    fun `tet at ty 2025 sequence`() {
        assertTrue(names(LocalDate.of(2025, 1, 28)).contains("Giao thừa"))
        assertTrue(names(LocalDate.of(2025, 1, 29)).contains("Tết Nguyên Đán (Mùng 1)"))
        assertTrue(names(LocalDate.of(2025, 1, 30)).contains("Mùng 2 Tết"))
        assertTrue(names(LocalDate.of(2025, 1, 31)).contains("Mùng 3 Tết"))
    }

    @Test
    fun `solar holidays`() {
        assertTrue(names(LocalDate.of(2024, 9, 2)).contains("Quốc khánh"))
        assertTrue(names(LocalDate.of(2024, 1, 1)).contains("Tết Dương lịch"))
        assertTrue(names(LocalDate.of(2024, 4, 30)).contains("Giải phóng miền Nam"))
    }

    @Test
    fun `ram thang gieng 2024`() {
        assertTrue(names(LocalDate.of(2024, 2, 24)).contains("Rằm tháng Giêng"))
    }

    @Test
    fun `lunar holidays skip leap months`() {
        // 15/6 nhuận 2025: rằm of the leap month carries no holiday.
        val leapRam = LunarCalendar.lunarToSolar(15, 6, 2025, isLeapMonth = true)!!.toLocalDate()
        assertTrue(Holidays.on(leapRam).filterIsInstance<LunarHoliday>().isEmpty())
        // The regular month 6 already had Tết Đoan Ngọ? No — 5/5. Check regular 15/7 Vu Lan still resolves.
        val vuLan = LunarCalendar.lunarToSolar(15, 7, 2025)!!.toLocalDate()
        assertTrue(names(vuLan).contains("Lễ Vu Lan"))
    }

    @Test
    fun `plain days have no holidays`() {
        assertTrue(Holidays.on(LocalDate.of(2024, 7, 3)).isEmpty())
    }
}
