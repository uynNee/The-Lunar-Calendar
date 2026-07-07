package com.uynne.lunarcalendar.ui.day

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.data.calendar.CalendarEvent
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DayOfWeek.vn: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "Thứ Hai"
        DayOfWeek.TUESDAY -> "Thứ Ba"
        DayOfWeek.WEDNESDAY -> "Thứ Tư"
        DayOfWeek.THURSDAY -> "Thứ Năm"
        DayOfWeek.FRIDAY -> "Thứ Sáu"
        DayOfWeek.SATURDAY -> "Thứ Bảy"
        DayOfWeek.SUNDAY -> "Chủ Nhật"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    date: LocalDate,
    onBack: () -> Unit,
    onAddEvent: (LocalDate) -> Unit = {},
    onEditEvent: (Long) -> Unit = {},
    eventsViewModel: DayEventsViewModel = viewModel(factory = DayEventsViewModel.Factory),
) {
    val permission = rememberCalendarPermissionState()
    val events by eventsViewModel.events.collectAsStateWithLifecycle()
    val hasWritable by eventsViewModel.hasWritableCalendar.collectAsStateWithLifecycle()
    LaunchedEffect(date) { eventsViewModel.setDate(date) }
    LaunchedEffect(permission.granted) { eventsViewModel.setPermissionGranted(permission.granted) }

    val lunar = remember(date) { LunarCalendar.solarToLunar(date) }
    val yearCanChi = remember(lunar) { LunarCalendar.canChiOfYear(lunar.year) }
    val monthCanChi = remember(lunar) { LunarCalendar.canChiOfMonth(lunar.month, lunar.year) }
    val dayCanChi = remember(date) {
        LunarCalendar.canChiOfDay(date.dayOfMonth, date.monthValue, date.year)
    }
    val phase = remember(date) {
        LunarCalendar.moonPhase(date.dayOfMonth, date.monthValue, date.year)
    }
    val star = remember(lunar, dayCanChi) { LunarCalendar.dayStar(lunar.month, dayCanChi.chi) }
    val holidays = remember(date) { Holidays.on(date) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết ngày") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailCard {
                Text(
                    text = date.dayOfWeek.vn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year),
                    style = MaterialTheme.typography.displaySmall,
                )
                val leap = if (lunar.isLeapMonth) " nhuận" else ""
                Text(
                    text = "Ngày ${lunar.day} tháng ${lunar.month}$leap năm ${yearCanChi.display}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            DetailCard(title = "Can Chi") {
                InfoRow("Năm", yearCanChi.display)
                InfoRow("Tháng", monthCanChi.display + if (lunar.isLeapMonth) " (nhuận)" else "")
                InfoRow("Ngày", dayCanChi.display)
            }
            DetailCard(title = "Trăng và sao") {
                InfoRow("Trăng", phase.vn)
                val quality = if (star.quality == DayQuality.HOANG_DAO) "hoàng đạo" else "hắc đạo"
                val qualityColor = if (star.quality == DayQuality.HOANG_DAO) {
                    LocalExtendedColors.current.hoangDao
                } else {
                    LocalExtendedColors.current.hacDao
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sao",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${star.name} ($quality)",
                        color = qualityColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            DetailCard(title = "Sự kiện") {
                if (holidays.isEmpty() && events.isEmpty()) {
                    Text(
                        text = "Không có sự kiện",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                holidays.forEach { holiday ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = holiday.name,
                            fontWeight = FontWeight.Medium,
                            color = if (holiday.type == HolidayType.PUBLIC) {
                                LocalExtendedColors.current.holidayRed
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (holiday.type == HolidayType.PUBLIC) "Lễ chính" else "Truyền thống",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                events.forEach { event ->
                    EventRow(event = event, onClick = { onEditEvent(event.eventId) })
                }
                when {
                    !permission.granted -> {
                        TextButton(
                            onClick = if (permission.deniedOnce) permission.openSettings else permission.request,
                        ) {
                            Text(
                                if (permission.deniedOnce) {
                                    "Mở cài đặt để cho phép truy cập lịch"
                                } else {
                                    "Cho phép truy cập lịch"
                                },
                            )
                        }
                    }
                    hasWritable -> {
                        TextButton(onClick = { onAddEvent(date) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Thêm sự kiện", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, onClick: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    val timeLabel = if (event.allDay) {
        "Cả ngày"
    } else {
        val begin = Instant.ofEpochMilli(event.beginMillis).atZone(zone).toLocalTime()
        val end = Instant.ofEpochMilli(event.endMillis).atZone(zone).toLocalTime()
        "${timeFormat.format(begin)} – ${timeFormat.format(end)}"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(event.color), CircleShape),
        )
        Text(
            text = event.title,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        if (event.isRecurring) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Lặp lại",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 2.dp),
            )
        }
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailCard(title: String? = null, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun DayDetailPreview() {
    LunarCalendarTheme {
        DayDetailScreen(date = LocalDate.of(2025, 1, 29), onBack = {})
    }
}
