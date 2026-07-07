package com.uynne.lunarcalendar.data.calendar

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("calendar_prefs", Context.MODE_PRIVATE)

    private val _hiddenCalendarIds = MutableStateFlow(load())
    val hiddenCalendarIds: StateFlow<Set<Long>> = _hiddenCalendarIds.asStateFlow()

    fun setHidden(calendarId: Long, hidden: Boolean) {
        val updated = if (hidden) {
            _hiddenCalendarIds.value + calendarId
        } else {
            _hiddenCalendarIds.value - calendarId
        }
        _hiddenCalendarIds.value = updated
        prefs.edit()
            .putStringSet(KEY_HIDDEN, updated.map(Long::toString).toSet())
            .apply()
    }

    private fun load(): Set<Long> =
        prefs.getStringSet(KEY_HIDDEN, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()

    private companion object {
        const val KEY_HIDDEN = "hidden_calendar_ids"
    }
}
