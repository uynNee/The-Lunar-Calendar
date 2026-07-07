package com.uynne.lunarcalendar.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uynne.lunarcalendar.data.calendar.CalendarGraph
import com.uynne.lunarcalendar.data.calendar.CalendarRepository
import com.uynne.lunarcalendar.data.calendar.DeviceCalendar
import com.uynne.lunarcalendar.data.calendar.EventDraft
import com.uynne.lunarcalendar.data.calendar.buildWriteSpec
import com.uynne.lunarcalendar.data.calendar.isDraftValid
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventEditorViewModel(private val repository: CalendarRepository) : ViewModel() {

    data class UiState(
        val draft: EventDraft? = null,
        val writableCalendars: List<DeviceCalendar> = emptyList(),
        val isEdit: Boolean = false,
        val loading: Boolean = true,
        val saveError: Boolean = false,
    ) {
        val canSave: Boolean get() = draft != null && isDraftValid(draft) && !loading
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var editingEventId: Long = -1L
    private var initialized = false

    fun initialize(date: LocalDate, eventId: Long) {
        if (initialized) return
        initialized = true
        editingEventId = eventId
        viewModelScope.launch {
            val writable = repository.queryCalendars().filter { it.isWritable }
            val draft = if (eventId >= 0) {
                repository.queryEventDraft(eventId, ZoneId.systemDefault())
            } else {
                EventDraft(
                    title = "",
                    calendarId = writable.firstOrNull()?.id ?: 0L,
                    date = date,
                    allDay = false,
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(10, 0),
                    yearlyRepeat = false,
                )
            }
            _state.update {
                it.copy(
                    draft = draft,
                    writableCalendars = writable,
                    isEdit = eventId >= 0,
                    loading = false,
                )
            }
        }
    }

    fun update(transform: (EventDraft) -> EventDraft) {
        _state.update { current ->
            current.copy(draft = current.draft?.let(transform), saveError = false)
        }
    }

    fun save(onDone: () -> Unit) {
        val draft = _state.value.draft ?: return
        viewModelScope.launch {
            val spec = buildWriteSpec(draft, ZoneId.systemDefault())
            val ok = if (editingEventId >= 0) {
                repository.updateEvent(editingEventId, spec)
            } else {
                repository.insertEvent(spec) != null
            }
            if (ok) onDone() else _state.update { it.copy(saveError = true) }
        }
    }

    fun delete(onDone: () -> Unit) {
        if (editingEventId < 0) return
        viewModelScope.launch {
            if (repository.deleteEvent(editingEventId)) {
                onDone()
            } else {
                _state.update { it.copy(saveError = true) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                EventEditorViewModel(CalendarGraph.repository(app))
            }
        }
    }
}
