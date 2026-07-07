package com.uynne.lunarcalendar.data.calendar

import java.time.LocalDate
import java.time.LocalTime

data class DeviceCalendar(
    val id: Long,
    val name: String,
    val accountName: String,
    val color: Int,
    val isVisible: Boolean,
    val isWritable: Boolean,
)

data class CalendarEvent(
    val instanceId: Long,
    val eventId: Long,
    val calendarId: Long,
    val title: String,
    val beginMillis: Long,
    val endMillis: Long,
    val allDay: Boolean,
    val color: Int,
    val isRecurring: Boolean,
)

/** Editor input. [startTime]/[endTime] are ignored when [allDay]. */
data class EventDraft(
    val title: String,
    val calendarId: Long,
    val date: LocalDate,
    val allDay: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val yearlyRepeat: Boolean,
)

/** Provider-ready write values. Recurring events carry [duration] instead of [dtEndMillis]. */
data class EventWriteSpec(
    val calendarId: Long,
    val title: String,
    val dtStartMillis: Long,
    val dtEndMillis: Long?,
    val duration: String?,
    val allDay: Boolean,
    val timeZoneId: String,
    val rrule: String?,
)
