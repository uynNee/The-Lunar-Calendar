package com.uynne.lunarcalendar.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uynne.lunarcalendar.data.calendar.CalendarEvent
import com.uynne.lunarcalendar.data.calendar.CalendarGraph
import com.uynne.lunarcalendar.data.calendar.CalendarPrefs
import com.uynne.lunarcalendar.data.calendar.CalendarRepository
import com.uynne.lunarcalendar.data.calendar.groupEventsByDate
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class DayEventsViewModel(
    private val repository: CalendarRepository,
    prefs: CalendarPrefs,
) : ViewModel() {

    private val date = MutableStateFlow<LocalDate?>(null)
    private val permissionGranted = MutableStateFlow(false)

    // Observer subscription gated on permission — see MonthViewModel.eventDates.
    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<CalendarEvent>> =
        permissionGranted
            .flatMapLatest { granted ->
                if (!granted) {
                    flowOf(emptyList())
                } else {
                    combine(
                        date.filterNotNull(),
                        prefs.hiddenCalendarIds,
                        repository.changes().onStart { emit(Unit) },
                    ) { day, hidden, _ -> day to hidden }
                        .map { (day, hidden) ->
                            val zone = ZoneId.systemDefault()
                            val instances = repository.queryInstances(day, day.plusDays(1), zone)
                                .filter { it.calendarId !in hidden }
                            groupEventsByDate(instances, zone)[day].orEmpty()
                        }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val hasWritableCalendar: StateFlow<Boolean> =
        permissionGranted
            .map { granted -> granted && repository.queryCalendars().any { it.isWritable } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setDate(value: LocalDate) {
        date.value = value
    }

    fun setPermissionGranted(granted: Boolean) {
        permissionGranted.value = granted
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                DayEventsViewModel(CalendarGraph.repository(app), CalendarGraph.prefs(app))
            }
        }
    }
}
