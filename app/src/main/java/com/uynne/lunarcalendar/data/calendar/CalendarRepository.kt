package com.uynne.lunarcalendar.data.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Instances
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class CalendarRepository(private val resolver: ContentResolver) {

    /** Emits whenever the CalendarProvider changes (sync, other apps, our own writes). */
    fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(CalendarContract.CONTENT_URI, true, observer)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    suspend fun queryCalendars(): List<DeviceCalendar> = withContext(Dispatchers.IO) {
        val calendars = mutableListOf<DeviceCalendar>()
        try {
            resolver.query(
                Calendars.CONTENT_URI,
                arrayOf(
                    Calendars._ID,
                    Calendars.CALENDAR_DISPLAY_NAME,
                    Calendars.ACCOUNT_NAME,
                    Calendars.CALENDAR_COLOR,
                    Calendars.VISIBLE,
                    Calendars.CALENDAR_ACCESS_LEVEL,
                ),
                null,
                null,
                "${Calendars.ACCOUNT_NAME} ASC, ${Calendars.CALENDAR_DISPLAY_NAME} ASC",
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    calendars += DeviceCalendar(
                        id = cursor.getLong(0),
                        name = cursor.getString(1).orEmpty(),
                        accountName = cursor.getString(2).orEmpty(),
                        color = cursor.getInt(3),
                        isVisible = cursor.getInt(4) == 1,
                        isWritable = cursor.getInt(5) >= Calendars.CAL_ACCESS_CONTRIBUTOR,
                    )
                }
            }
        } catch (_: SecurityException) {
        }
        calendars
    }

    suspend fun queryInstances(
        start: LocalDate,
        endExclusive: LocalDate,
        zone: ZoneId,
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<CalendarEvent>()
        val startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = endExclusive.atStartOfDay(zone).toInstant().toEpochMilli()
        val uri = Instances.CONTENT_URI.buildUpon()
            .appendPath(startMillis.toString())
            .appendPath(endMillis.toString())
            .build()
        try {
            resolver.query(
                uri,
                arrayOf(
                    Instances._ID,
                    Instances.EVENT_ID,
                    Instances.CALENDAR_ID,
                    Instances.TITLE,
                    Instances.BEGIN,
                    Instances.END,
                    Instances.ALL_DAY,
                    Instances.DISPLAY_COLOR,
                    Instances.RRULE,
                ),
                "${Instances.VISIBLE} = 1",
                null,
                "${Instances.BEGIN} ASC",
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    events += CalendarEvent(
                        instanceId = cursor.getLong(0),
                        eventId = cursor.getLong(1),
                        calendarId = cursor.getLong(2),
                        title = cursor.getString(3).orEmpty().ifBlank { "(Không có tiêu đề)" },
                        beginMillis = cursor.getLong(4),
                        endMillis = cursor.getLong(5),
                        allDay = cursor.getInt(6) == 1,
                        color = cursor.getInt(7),
                        isRecurring = !cursor.getString(8).isNullOrEmpty(),
                    )
                }
            }
        } catch (_: SecurityException) {
        }
        events
    }

    /** Loads one event row into an editor draft, or null when missing/unreadable. */
    suspend fun queryEventDraft(eventId: Long, zone: ZoneId): EventDraft? = withContext(Dispatchers.IO) {
        try {
            resolver.query(
                ContentUris.withAppendedId(Events.CONTENT_URI, eventId),
                arrayOf(
                    Events.TITLE,
                    Events.CALENDAR_ID,
                    Events.DTSTART,
                    Events.DTEND,
                    Events.DURATION,
                    Events.ALL_DAY,
                    Events.RRULE,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@withContext null
                eventRowToDraft(
                    title = cursor.getString(0),
                    calendarId = cursor.getLong(1),
                    dtStartMillis = cursor.getLong(2),
                    dtEndMillis = if (cursor.isNull(3)) null else cursor.getLong(3),
                    durationRfc = cursor.getString(4),
                    allDay = cursor.getInt(5) == 1,
                    rrule = cursor.getString(6),
                    zone = zone,
                )
            }
        } catch (_: SecurityException) {
            null
        }
    }

    suspend fun insertEvent(spec: EventWriteSpec): Long? = withContext(Dispatchers.IO) {
        try {
            resolver.insert(Events.CONTENT_URI, spec.toContentValues())
                ?.let(ContentUris::parseId)
        } catch (_: SecurityException) {
            null
        }
    }

    suspend fun updateEvent(eventId: Long, spec: EventWriteSpec): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(Events.CONTENT_URI, eventId)
            resolver.update(uri, spec.toContentValues(), null, null) > 0
        } catch (_: SecurityException) {
            false
        }
    }

    suspend fun deleteEvent(eventId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(Events.CONTENT_URI, eventId)
            resolver.delete(uri, null, null) > 0
        } catch (_: SecurityException) {
            false
        }
    }

    /** Vacated columns are set to null so single<->yearly updates don't leave stale DTEND/RRULE. */
    private fun EventWriteSpec.toContentValues() = ContentValues().apply {
        put(Events.CALENDAR_ID, calendarId)
        put(Events.TITLE, title)
        put(Events.DTSTART, dtStartMillis)
        put(Events.EVENT_TIMEZONE, timeZoneId)
        put(Events.ALL_DAY, if (allDay) 1 else 0)
        if (dtEndMillis != null) put(Events.DTEND, dtEndMillis) else putNull(Events.DTEND)
        if (duration != null) put(Events.DURATION, duration) else putNull(Events.DURATION)
        if (rrule != null) put(Events.RRULE, rrule) else putNull(Events.RRULE)
    }
}
