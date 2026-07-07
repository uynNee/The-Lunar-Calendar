package com.uynne.lunarcalendar.data.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class EventMappingTest {

    private val vn: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    private fun timedMillis(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(vn).toInstant().toEpochMilli()

    private fun event(
        begin: Long,
        end: Long,
        allDay: Boolean = false,
        title: String = "e",
        id: Long = 1L,
    ) = CalendarEvent(
        instanceId = id, eventId = id, calendarId = 1L, title = title,
        beginMillis = begin, endMillis = end, allDay = allDay, color = 0, isRecurring = false,
    )

    @Test
    fun `timed event maps to single date`() {
        val date = LocalDate.of(2026, 8, 15)
        val dates = instanceDates(timedMillis(date, 9), timedMillis(date, 10), false, vn)
        assertEquals(listOf(date), dates)
    }

    @Test
    fun `timed event across midnight spans two dates`() {
        val date = LocalDate.of(2026, 8, 15)
        val dates = instanceDates(
            timedMillis(date, 23),
            timedMillis(date.plusDays(1), 1),
            false,
            vn,
        )
        assertEquals(listOf(date, date.plusDays(1)), dates)
    }

    @Test
    fun `timed event ending exactly at midnight stays on one date`() {
        val date = LocalDate.of(2026, 8, 15)
        val dates = instanceDates(
            timedMillis(date, 22),
            date.plusDays(1).atStartOfDay(vn).toInstant().toEpochMilli(),
            false,
            vn,
        )
        assertEquals(listOf(date), dates)
    }

    @Test
    fun `zero length instance maps to one date`() {
        val date = LocalDate.of(2026, 8, 15)
        val at = timedMillis(date, 9)
        assertEquals(listOf(date), instanceDates(at, at, false, vn))
    }

    @Test
    fun `all day event interpreted at utc in any device zone`() {
        val date = LocalDate.of(2026, 8, 15)
        val beginUtc = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        val endUtc = date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(listOf(date), instanceDates(beginUtc, endUtc, true, vn))
        assertEquals(listOf(date), instanceDates(beginUtc, endUtc, true, ZoneId.of("America/New_York")))
    }

    @Test
    fun `multi day all day event is end exclusive`() {
        val date = LocalDate.of(2026, 8, 15)
        val beginUtc = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        val endUtc = date.plusDays(3).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(
            listOf(date, date.plusDays(1), date.plusDays(2)),
            instanceDates(beginUtc, endUtc, true, vn),
        )
    }

    @Test
    fun `grouping sorts all day before timed`() {
        val date = LocalDate.of(2026, 8, 15)
        val timed = event(timedMillis(date, 8), timedMillis(date, 9), title = "timed", id = 1)
        val allDay = event(
            date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
            date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
            allDay = true,
            title = "allday",
            id = 2,
        )
        val grouped = groupEventsByDate(listOf(timed, allDay), vn)
        assertEquals(listOf("allday", "timed"), grouped.getValue(date).map { it.title })
    }

    @Test
    fun `rfc duration parsing`() {
        assertEquals(86_400_000L, parseRfcDurationMillis("P1D"))
        assertEquals(5_400_000L, parseRfcDurationMillis("PT5400S"))
        assertEquals(3_600_000L, parseRfcDurationMillis("PT1H"))
        assertEquals(1_209_600_000L, parseRfcDurationMillis("P2W"))
        assertEquals(3_600_000L, parseRfcDurationMillis(null))
        assertTrue(parseRfcDurationMillis("garbage") > 0)
    }
}
