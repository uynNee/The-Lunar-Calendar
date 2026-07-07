package com.uynne.lunarcalendar.ui.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.components.EventListRow
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import java.time.DayOfWeek
import java.time.LocalDate

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
    val leap = if (lunar.isLeapMonth) " nhuận" else ""

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết ngày", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
            ) {
                Text(
                    text = date.dayOfWeek.vn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Ngày ${lunar.day} tháng ${lunar.month}$leap năm ${yearCanChi.display}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            GroupedSection(title = "Can Chi") {
                GroupedRow("Năm", yearCanChi.display)
                RowDivider()
                GroupedRow("Tháng", monthCanChi.display + if (lunar.isLeapMonth) " (nhuận)" else "")
                RowDivider()
                GroupedRow("Ngày", dayCanChi.display)
            }

            GroupedSection(title = "Trăng và sao") {
                GroupedRow("Trăng", phase.vn)
                RowDivider()
                val quality = if (star.quality == DayQuality.HOANG_DAO) "hoàng đạo" else "hắc đạo"
                val qualityColor = if (star.quality == DayQuality.HOANG_DAO) {
                    LocalExtendedColors.current.hoangDao
                } else {
                    LocalExtendedColors.current.hacDao
                }
                GroupedRow(
                    label = "Sao",
                    value = "${star.name} ($quality)",
                    valueColor = qualityColor,
                )
            }

            GroupedSection(title = "Sự kiện") {
                if (holidays.isEmpty() && events.isEmpty()) {
                    Text(
                        text = "Không có sự kiện",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }
                holidays.forEachIndexed { index, holiday ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = holiday.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (holiday.type == HolidayType.PUBLIC) {
                                LocalExtendedColors.current.holidayRed
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (holiday.type == HolidayType.PUBLIC) "Lễ chính" else "Truyền thống",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (index != holidays.lastIndex || events.isNotEmpty()) RowDivider()
                }
                events.forEachIndexed { index, event ->
                    EventListRow(event = event, onClick = { onEditEvent(event.eventId) })
                    if (index != events.lastIndex) RowDivider(modifier = Modifier.padding(start = 21.dp))
                }
                when {
                    !permission.granted -> {
                        RowDivider()
                        TextButton(
                            onClick = if (permission.deniedOnce) permission.openSettings else permission.request,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                        RowDivider()
                        TextButton(
                            onClick = { onAddEvent(date) },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text("+ Thêm sự kiện")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DayDetailPreview() {
    LunarCalendarTheme {
        DayDetailScreen(date = LocalDate.of(2025, 1, 29), onBack = {})
    }
}
