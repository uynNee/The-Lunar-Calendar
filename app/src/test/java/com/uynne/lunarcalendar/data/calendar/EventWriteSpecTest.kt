package com.uynne.lunarcalendar.data.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class EventWriteSpecTest {

    private val vn: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    private val baseDraft = EventDraft(
        title = "Họp",
        calendarId = 5L,
        date = LocalDate.of(2026, 8, 15),
        allDay = false,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(10, 30),
        yearlyRepeat = false,
    )

    @Test
    fun `single timed event`() {
        val spec = buildWriteSpec(baseDraft, vn)
        assertEquals(
            baseDraft.date.atTime(9, 0).atZone(vn).toInstant().toEpochMilli(),
            spec.dtStartMillis,
        )
        assertEquals(
            baseDraft.date.atTime(10, 30).atZone(vn).toInstant().toEpochMilli(),
            spec.dtEndMillis,
        )
        assertEquals(vn.id, spec.timeZoneId)
        assertNull(spec.duration)
        assertNull(spec.rrule)
        assertFalse(spec.allDay)
    }

    @Test
    fun `single all day event anchored to utc`() {
        val spec = buildWriteSpec(baseDraft.copy(allDay = true), vn)
        val utcMidnight = baseDraft.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(utcMidnight, spec.dtStartMillis)
        assertEquals(utcMidnight + 86_400_000L, spec.dtEndMillis)
        assertEquals("UTC", spec.timeZoneId)
        assertTrue(spec.allDay)
        assertNull(spec.rrule)
    }

    @Test
    fun `yearly timed event uses duration not dtend`() {
        val spec = buildWriteSpec(baseDraft.copy(yearlyRepeat = true), vn)
        assertNull(spec.dtEndMillis)
        assertEquals("PT5400S", spec.duration)
        assertEquals("FREQ=YEARLY", spec.rrule)
    }

    @Test
    fun `yearly all day event`() {
        val spec = buildWriteSpec(baseDraft.copy(allDay = true, yearlyRepeat = true), vn)
        assertNull(spec.dtEndMillis)
        assertEquals("P1D", spec.duration)
        assertEquals("FREQ=YEARLY", spec.rrule)
        assertEquals("UTC", spec.timeZoneId)
    }

    @Test
    fun `end before start coerced to one hour`() {
        val spec = buildWriteSpec(baseDraft.copy(endTime = LocalTime.of(8, 0)), vn)
        assertEquals(spec.dtStartMillis + 3_600_000L, spec.dtEndMillis)
    }

    @Test
    fun `draft validation`() {
        assertTrue(isDraftValid(baseDraft))
        assertFalse(isDraftValid(baseDraft.copy(title = "   ")))
        assertFalse(isDraftValid(baseDraft.copy(calendarId = 0)))
    }

    @Test
    fun `write spec round trips to draft`() {
        val variants = listOf(
            baseDraft,
            baseDraft.copy(allDay = true),
            baseDraft.copy(yearlyRepeat = true),
        )
        for (draft in variants) {
            val spec = buildWriteSpec(draft, vn)
            val restored = eventRowToDraft(
                title = spec.title,
                calendarId = spec.calendarId,
                dtStartMillis = spec.dtStartMillis,
                dtEndMillis = spec.dtEndMillis,
                durationRfc = spec.duration,
                allDay = spec.allDay,
                rrule = spec.rrule,
                zone = vn,
            )
            assertEquals(draft.title, restored.title)
            assertEquals(draft.calendarId, restored.calendarId)
            assertEquals(draft.date, restored.date)
            assertEquals(draft.allDay, restored.allDay)
            assertEquals(draft.yearlyRepeat, restored.yearlyRepeat)
            if (!draft.allDay) {
                assertEquals(draft.startTime, restored.startTime)
                assertEquals(draft.endTime, restored.endTime)
            }
        }
    }
}
