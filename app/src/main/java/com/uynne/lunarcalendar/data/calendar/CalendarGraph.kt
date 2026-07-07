package com.uynne.lunarcalendar.data.calendar

import android.content.Context

/** Lazy singletons for the calendar data layer; no DI framework needed at this size. */
object CalendarGraph {

    @Volatile
    private var repository: CalendarRepository? = null

    @Volatile
    private var prefs: CalendarPrefs? = null

    fun repository(context: Context): CalendarRepository =
        repository ?: synchronized(this) {
            repository ?: CalendarRepository(context.applicationContext.contentResolver)
                .also { repository = it }
        }

    fun prefs(context: Context): CalendarPrefs =
        prefs ?: synchronized(this) {
            prefs ?: CalendarPrefs(context.applicationContext).also { prefs = it }
        }
}
