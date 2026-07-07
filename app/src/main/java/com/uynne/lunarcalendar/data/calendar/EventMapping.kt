package com.uynne.lunarcalendar.data.calendar

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

private const val YEARLY_RRULE = "FREQ=YEARLY"

/**
 * Dates an instance covers, end-exclusive. All-day instances are stored as UTC
 * midnights and must be interpreted at UTC regardless of device zone; timed
 * instances follow [zone].
 */
fun instanceDates(beginMillis: Long, endMillis: Long, allDay: Boolean, zone: ZoneId): List<LocalDate> {
    val effectiveZone = if (allDay) ZoneOffset.UTC else zone
    val start = Instant.ofEpochMilli(beginMillis).atZone(effectiveZone).toLocalDate()
    val endInstant = Instant.ofEpochMilli(endMillis).atZone(effectiveZone)
    // End is exclusive: an event ending exactly at midnight does not cover that day.
    var endDate = endInstant.toLocalDate()
    if (endInstant.toLocalTime() == LocalTime.MIDNIGHT && endDate.isAfter(start)) {
        endDate = endDate.minusDays(1)
    }
    if (endDate.isBefore(start)) endDate = start
    return generateSequence(start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(endDate) }
        .toList()
}

/** Groups events onto each date they cover; all-day events sort before timed ones. */
fun groupEventsByDate(events: List<CalendarEvent>, zone: ZoneId): Map<LocalDate, List<CalendarEvent>> {
    val byDate = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()
    for (event in events) {
        for (date in instanceDates(event.beginMillis, event.endMillis, event.allDay, zone)) {
            byDate.getOrPut(date) { mutableListOf() } += event
        }
    }
    return byDate.mapValues { (_, list) ->
        list.sortedWith(compareByDescending<CalendarEvent> { it.allDay }.thenBy { it.beginMillis })
    }
}

fun isDraftValid(draft: EventDraft): Boolean =
    draft.title.isNotBlank() && draft.calendarId > 0

/**
 * CalendarProvider rules: recurring events must carry DURATION (RFC 2445) and no
 * DTEND; single events carry DTEND and no DURATION. All-day events are anchored
 * to UTC midnights with timezone "UTC".
 */
fun buildWriteSpec(draft: EventDraft, zone: ZoneId): EventWriteSpec {
    val start: Long
    val end: Long
    val timeZoneId: String
    if (draft.allDay) {
        start = draft.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        end = draft.date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        timeZoneId = "UTC"
    } else {
        val endTime = if (draft.endTime > draft.startTime) draft.endTime else null
        start = draft.date.atTime(draft.startTime).atZone(zone).toInstant().toEpochMilli()
        end = if (endTime != null) {
            draft.date.atTime(endTime).atZone(zone).toInstant().toEpochMilli()
        } else {
            start + Duration.ofHours(1).toMillis()
        }
        timeZoneId = zone.id
    }
    return if (draft.yearlyRepeat) {
        EventWriteSpec(
            calendarId = draft.calendarId,
            title = draft.title.trim(),
            dtStartMillis = start,
            dtEndMillis = null,
            duration = if (draft.allDay) "P1D" else "PT${(end - start) / 1000}S",
            allDay = draft.allDay,
            timeZoneId = timeZoneId,
            rrule = YEARLY_RRULE,
        )
    } else {
        EventWriteSpec(
            calendarId = draft.calendarId,
            title = draft.title.trim(),
            dtStartMillis = start,
            dtEndMillis = end,
            duration = null,
            allDay = draft.allDay,
            timeZoneId = timeZoneId,
            rrule = null,
        )
    }
}

/** Inverse of [buildWriteSpec] for loading an event row into the editor. */
fun eventRowToDraft(
    title: String?,
    calendarId: Long,
    dtStartMillis: Long,
    dtEndMillis: Long?,
    durationRfc: String?,
    allDay: Boolean,
    rrule: String?,
    zone: ZoneId,
): EventDraft {
    val yearly = rrule?.contains(YEARLY_RRULE) == true
    val effectiveZone = if (allDay) ZoneOffset.UTC else zone
    val start = Instant.ofEpochMilli(dtStartMillis).atZone(effectiveZone)
    val endMillis = dtEndMillis ?: (dtStartMillis + parseRfcDurationMillis(durationRfc))
    val end = Instant.ofEpochMilli(endMillis).atZone(effectiveZone)
    return EventDraft(
        title = title.orEmpty(),
        calendarId = calendarId,
        date = start.toLocalDate(),
        allDay = allDay,
        startTime = if (allDay) LocalTime.of(9, 0) else start.toLocalTime(),
        endTime = if (allDay) LocalTime.of(10, 0) else end.toLocalTime(),
        yearlyRepeat = yearly,
    )
}

/** Parses the RFC 2445 duration subset the provider uses ("P1D", "PT3600S", "P2W"). */
internal fun parseRfcDurationMillis(duration: String?): Long {
    if (duration.isNullOrBlank()) return Duration.ofHours(1).toMillis()
    val match = Regex("^P(?:(\\d+)W)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?$")
        .find(duration.trim()) ?: return Duration.ofHours(1).toMillis()
    val (weeks, days, hours, minutes, seconds) = match.destructured
    return Duration.ofDays((weeks.toLongOrNull() ?: 0) * 7 + (days.toLongOrNull() ?: 0))
        .plusHours(hours.toLongOrNull() ?: 0)
        .plusMinutes(minutes.toLongOrNull() ?: 0)
        .plusSeconds(seconds.toLongOrNull() ?: 0)
        .toMillis()
}
