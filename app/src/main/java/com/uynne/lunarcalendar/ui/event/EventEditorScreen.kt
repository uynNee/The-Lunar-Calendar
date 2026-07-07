package com.uynne.lunarcalendar.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.data.calendar.EventDraft
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(
    date: LocalDate,
    eventId: Long,
    onClose: () -> Unit,
    viewModel: EventEditorViewModel = viewModel(factory = EventEditorViewModel.Factory),
) {
    LaunchedEffect(Unit) { viewModel.initialize(date, eventId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEdit) "Sửa sự kiện" else "Sự kiện mới",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onClose) { Text("Hủy") }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(onClose) },
                        enabled = state.canSave,
                    ) {
                        Text("Lưu", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        val draft = state.draft
        if (state.loading || draft == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                if (state.loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Không tải được sự kiện")
                }
            }
            return@Scaffold
        }
        EditorForm(
            draft = draft,
            state = state,
            onUpdate = viewModel::update,
            onDelete = if (state.isEdit) ({ confirmDelete = true }) else null,
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Xoá sự kiện này?") },
            text = { Text("Sự kiện lặp lại sẽ bị xoá toàn bộ chuỗi.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onClose)
                }) {
                    Text("Xoá", color = LocalExtendedColors.current.holidayRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Hủy") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorForm(
    draft: EventDraft,
    state: EventEditorViewModel.UiState,
    onUpdate: ((EventDraft) -> EventDraft) -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.saveError) {
            Text(
                text = "Không lưu được sự kiện. Thử lại.",
                color = LocalExtendedColors.current.holidayRed,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state.writableCalendars.isEmpty()) {
            Text(
                text = "Không có lịch nào cho phép ghi. Thêm tài khoản Google có Lịch trước.",
                color = LocalExtendedColors.current.holidayRed,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        OutlinedTextField(
            value = draft.title,
            onValueChange = { value -> onUpdate { it.copy(title = value) } },
            placeholder = { Text("Tiêu đề") },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        GroupedSection {
            GroupedRow(
                label = "Cả ngày",
                trailing = {
                    Switch(
                        checked = draft.allDay,
                        onCheckedChange = { value -> onUpdate { it.copy(allDay = value) } },
                    )
                },
            )
            RowDivider()
            GroupedRow(
                label = "Ngày",
                value = dateFormat.format(draft.date),
                valueColor = MaterialTheme.colorScheme.primary,
                onClick = { showDatePicker = true },
            )
            if (!draft.allDay) {
                RowDivider()
                GroupedRow(
                    label = "Bắt đầu",
                    value = timeFormat.format(draft.startTime),
                    valueColor = MaterialTheme.colorScheme.primary,
                    onClick = { showStartPicker = true },
                )
                RowDivider()
                GroupedRow(
                    label = "Kết thúc",
                    value = timeFormat.format(draft.endTime),
                    valueColor = MaterialTheme.colorScheme.primary,
                    onClick = { showEndPicker = true },
                )
            }
        }

        GroupedSection {
            CalendarDropdown(state = state, draft = draft, onUpdate = onUpdate)
            RowDivider()
            GroupedRow(
                label = "Lặp lại hằng năm",
                trailing = {
                    Switch(
                        checked = draft.yearlyRepeat,
                        onCheckedChange = { value -> onUpdate { it.copy(yearlyRepeat = value) } },
                    )
                },
            )
        }

        if (onDelete != null) {
            GroupedSection {
                GroupedRow(
                    label = "Xoá sự kiện",
                    valueColor = LocalExtendedColors.current.holidayRed,
                    onClick = onDelete,
                    trailing = {
                        Text(
                            text = "Xoá",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalExtendedColors.current.holidayRed,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = draft.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onUpdate { it.copy(date = picked) }
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
    if (showStartPicker) {
        TimePickerDialog(
            initial = draft.startTime,
            title = "Giờ bắt đầu",
            onDismiss = { showStartPicker = false },
            onConfirm = { time ->
                onUpdate {
                    val shift = java.time.Duration.between(it.startTime, it.endTime)
                    it.copy(startTime = time, endTime = time.plus(shift))
                }
                showStartPicker = false
            },
        )
    }
    if (showEndPicker) {
        TimePickerDialog(
            initial = draft.endTime,
            title = "Giờ kết thúc",
            onDismiss = { showEndPicker = false },
            onConfirm = { time ->
                onUpdate { it.copy(endTime = time) }
                showEndPicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDropdown(
    state: EventEditorViewModel.UiState,
    draft: EventDraft,
    onUpdate: ((EventDraft) -> EventDraft) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.writableCalendars.firstOrNull { it.id == draft.calendarId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Lịch",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    selected?.let {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(it.color)),
                        )
                    }
                    Text(
                        text = selected?.name ?: "Chọn lịch",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
                selected?.let {
                    Text(
                        text = it.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.writableCalendars.forEach { calendar ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(calendar.color)),
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(calendar.name)
                                Text(
                                    calendar.accountName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    onClick = {
                        onUpdate { it.copy(calendarId = calendar.id) }
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            Button(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text("Chọn")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}
