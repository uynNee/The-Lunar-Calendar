package com.uynne.lunarcalendar.data.calendar

import android.content.Context
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("calendar_prefs", Context.MODE_PRIVATE)

    private val _hiddenCalendarIds = MutableStateFlow(load())
    val hiddenCalendarIds: StateFlow<Set<Long>> = _hiddenCalendarIds.asStateFlow()

    private val _weekStart = MutableStateFlow(loadWeekStart())
    val weekStart: StateFlow<DayOfWeek> = _weekStart.asStateFlow()

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

    fun setWeekStart(day: DayOfWeek) {
        _weekStart.value = day
        prefs.edit().putString(KEY_WEEK_START, day.name).apply()
    }

    private fun load(): Set<Long> =
        prefs.getStringSet(KEY_HIDDEN, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()

    private fun loadWeekStart(): DayOfWeek =
        prefs.getString(KEY_WEEK_START, null)?.let { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }
            ?: DayOfWeek.MONDAY

    private companion object {
        const val KEY_HIDDEN = "hidden_calendar_ids"
        const val KEY_WEEK_START = "week_start"
    }
}
