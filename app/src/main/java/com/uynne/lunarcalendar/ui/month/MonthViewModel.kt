package com.uynne.lunarcalendar.ui.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uynne.lunarcalendar.data.calendar.CalendarPrefs
import com.uynne.lunarcalendar.data.calendar.CalendarGraph
import com.uynne.lunarcalendar.data.calendar.CalendarRepository
import com.uynne.lunarcalendar.data.calendar.DeviceCalendar
import com.uynne.lunarcalendar.data.calendar.groupEventsByDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonthViewModel(
    private val repository: CalendarRepository,
    private val prefs: CalendarPrefs,
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())
    private val permissionGranted = MutableStateFlow(false)

    val hiddenIds: StateFlow<Set<Long>> = prefs.hiddenCalendarIds

    private val _calendars = MutableStateFlow<List<DeviceCalendar>>(emptyList())
    val calendars: StateFlow<List<DeviceCalendar>> = _calendars

    @OptIn(FlowPreview::class)
    val eventDates: StateFlow<Map<LocalDate, Int>> =
        combine(
            visibleMonth,
            permissionGranted,
            prefs.hiddenCalendarIds,
            repository.changes().onStart { emit(Unit) },
        ) { month, granted, hidden, _ -> Triple(month, granted, hidden) }
            .debounce(250)
            .map { (month, granted, hidden) ->
                if (!granted) return@map emptyMap()
                val zone = ZoneId.systemDefault()
                val events = repository
                    .queryInstances(gridStart(month.minusMonths(1)), gridEndExclusive(month.plusMonths(1)), zone)
                    .filter { it.calendarId !in hidden }
                groupEventsByDate(events, zone).mapValues { it.value.size }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setVisibleMonth(month: YearMonth) {
        visibleMonth.value = month
    }

    fun setPermissionGranted(granted: Boolean) {
        if (permissionGranted.value != granted) {
            permissionGranted.value = granted
            if (granted) refreshCalendars()
        }
    }

    fun toggleCalendar(calendarId: Long, hidden: Boolean) {
        prefs.setHidden(calendarId, hidden)
    }

    fun refreshCalendars() {
        viewModelScope.launch { _calendars.value = repository.queryCalendars() }
    }

    companion object {
        /** Mirrors buildMonthGrid's Monday-first 6x7 window. */
        fun gridStart(month: YearMonth): LocalDate {
            val first = month.atDay(1)
            return first.minusDays((first.dayOfWeek.value - 1).toLong())
        }

        fun gridEndExclusive(month: YearMonth): LocalDate = gridStart(month).plusDays(42)

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                MonthViewModel(CalendarGraph.repository(app), CalendarGraph.prefs(app))
            }
        }
    }
}
