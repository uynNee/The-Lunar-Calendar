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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val weekStart: StateFlow<DayOfWeek> = prefs.weekStart

    private val _calendars = MutableStateFlow<List<DeviceCalendar>>(emptyList())
    val calendars: StateFlow<List<DeviceCalendar>> = _calendars

    // The provider observer may only be subscribed once permission is granted:
    // registerContentObserver on the calendar URI throws SecurityException without
    // READ_CALENDAR. flatMapLatest also re-registers it right after a grant.
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val eventDates: StateFlow<Map<LocalDate, Int>> =
        permissionGranted
            .flatMapLatest { granted ->
                if (!granted) {
                    flowOf(emptyMap())
                } else {
                    combine(
                        visibleMonth,
                        prefs.hiddenCalendarIds,
                        prefs.weekStart,
                        repository.changes().onStart { emit(Unit) },
                    ) { month, hidden, weekStart, _ -> Triple(month, hidden, weekStart) }
                        .debounce(250)
                        .map { (month, hidden, weekStart) ->
                            val zone = ZoneId.systemDefault()
                            val events = repository
                                .queryInstances(
                                    gridStart(month.minusMonths(1), weekStart),
                                    gridEndExclusive(month.plusMonths(1), weekStart),
                                    zone,
                                )
                                .filter { it.calendarId !in hidden }
                            groupEventsByDate(events, zone).mapValues { it.value.size }
                        }
                }
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
        /** Mirrors buildMonthGrid's 6x7 window. */
        fun gridStart(month: YearMonth, weekStart: DayOfWeek): LocalDate {
            val first = month.atDay(1)
            val offset = (first.dayOfWeek.value - weekStart.value + 7) % 7
            return first.minusDays(offset.toLong())
        }

        fun gridEndExclusive(month: YearMonth, weekStart: DayOfWeek): LocalDate =
            gridStart(month, weekStart).plusDays(42)

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                MonthViewModel(CalendarGraph.repository(app), CalendarGraph.prefs(app))
            }
        }
    }
}
