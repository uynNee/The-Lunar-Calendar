package com.uynne.lunarcalendar.lunar

import org.junit.Assert.assertEquals
import org.junit.Test

class AuspiciousDayTest {

    @Test
    fun `star table anchors`() {
        assertEquals("Thanh Long", LunarCalendar.dayStar(1, Chi.TY).name)
        assertEquals(DayQuality.HOANG_DAO, LunarCalendar.dayStar(1, Chi.TY).quality)
        assertEquals("Thiên Hình", LunarCalendar.dayStar(1, Chi.DAN).name)
        assertEquals(DayQuality.HAC_DAO, LunarCalendar.dayStar(1, Chi.DAN).quality)
        assertEquals("Thanh Long", LunarCalendar.dayStar(2, Chi.DAN).name)
    }

    @Test
    fun `months six apart share the same table`() {
        for (month in 1..6) {
            for (chi in Chi.values()) {
                assertEquals(
                    "month $month chi $chi",
                    LunarCalendar.dayStar(month, chi),
                    LunarCalendar.dayStar(month + 6, chi),
                )
            }
        }
    }

    @Test
    fun `exactly six hoang dao chi per month`() {
        for (month in 1..12) {
            val good = Chi.values().count {
                LunarCalendar.dayStar(month, it).quality == DayQuality.HOANG_DAO
            }
            assertEquals("month $month", 6, good)
        }
    }

    @Test
    fun `day quality integrates lunar month and day chi`() {
        // 2024-02-10 = mùng 1 Tết, lunar month 1, day chi Thìn → Kim Quỹ (hoàng đạo).
        assertEquals(DayQuality.HOANG_DAO, LunarCalendar.dayQuality(10, 2, 2024))
    }
}
