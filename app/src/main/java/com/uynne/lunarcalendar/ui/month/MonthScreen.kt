package com.uynne.lunarcalendar.ui.month

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.calendar.buildMonthGrid
import com.uynne.lunarcalendar.data.Holiday
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.data.calendar.CalendarEvent
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.calendars.CalendarPickerSheet
import com.uynne.lunarcalendar.ui.components.EventListRow
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 12_000
private const val INITIAL_PAGE = PAGE_COUNT / 2

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

@Composable
fun MonthScreen(
    today: LocalDate,
    onOpenDayDetail: (LocalDate) -> Unit,
    onAddEvent: (LocalDate) -> Unit,
    onEditEvent: (Long, LocalDate) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: MonthViewModel = viewModel(factory = MonthViewModel.Factory),
    dayEventsViewModel: com.uynne.lunarcalendar.ui.day.DayEventsViewModel =
        viewModel(factory = com.uynne.lunarcalendar.ui.day.DayEventsViewModel.Factory),
) {
    val baseMonth = remember(today) { YearMonth.from(today) }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { PAGE_COUNT }
    val scope = rememberCoroutineScope()
    val currentMonth = baseMonth.plusMonths((pagerState.currentPage - INITIAL_PAGE).toLong())
    var selectedDate by remember(today) { mutableStateOf(today) }
    var displayMode by remember { mutableStateOf(CalendarDisplayMode.MONTH) }

    val lunarYear = remember(currentMonth) {
        LunarCalendar.solarToLunar(currentMonth.atDay(15)).year
    }
    val selectedLunar = remember(selectedDate) { LunarCalendar.solarToLunar(selectedDate) }
    val selectedYearCanChi = remember(selectedLunar) { LunarCalendar.canChiOfYear(selectedLunar.year) }
    val selectedHolidays = remember(selectedDate) { Holidays.on(selectedDate) }

    val permission = rememberCalendarPermissionState()
    val eventDates by viewModel.eventDates.collectAsStateWithLifecycle()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val hiddenIds by viewModel.hiddenIds.collectAsStateWithLifecycle()
    val selectedEvents by dayEventsViewModel.events.collectAsStateWithLifecycle()
    val hasWritable by dayEventsViewModel.hasWritableCalendar.collectAsStateWithLifecycle()
    var showCalendarPicker by remember { mutableStateOf(false) }

    LaunchedEffect(permission.granted) {
        viewModel.setPermissionGranted(permission.granted)
        dayEventsViewModel.setPermissionGranted(permission.granted)
    }
    LaunchedEffect(currentMonth) {
        viewModel.setVisibleMonth(currentMonth)
        if (YearMonth.from(selectedDate) != currentMonth) {
            selectedDate = currentMonth.atDay(minOf(selectedDate.dayOfMonth, currentMonth.lengthOfMonth()))
        }
    }
    LaunchedEffect(selectedDate) { dayEventsViewModel.setDate(selectedDate) }

    val openCalendarPicker: (() -> Unit)? = if (permission.granted) {
        {
            viewModel.refreshCalendars()
            showCalendarPicker = true
        }
    } else {
        null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomCalendarBar(
                showToday = pagerState.currentPage != INITIAL_PAGE || selectedDate != today,
                onToday = {
                    selectedDate = today
                    displayMode = CalendarDisplayMode.MONTH
                    scope.launch { pagerState.animateScrollToPage(INITIAL_PAGE) }
                },
                onCalendarPicker = openCalendarPicker,
                onSettings = onOpenSettings,
                onAddEvent = { onAddEvent(selectedDate) },
                canAddEvent = permission.granted && hasWritable,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .animateContentSize(),
        ) {
            MonthHeader(
                month = currentMonth,
                lunarYearLabel = LunarCalendar.canChiOfYear(lunarYear).display,
            )
            if (!permission.granted) {
                PermissionBanner(
                    deniedOnce = permission.deniedOnce,
                    onRequest = permission.request,
                    onOpenSettings = permission.openSettings,
                )
            }
            WeekdayHeader()
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val month = baseMonth.plusMonths((page - INITIAL_PAGE).toLong())
                val grid = remember(page, today) { buildMonthGrid(month, today) }
                Crossfade(targetState = displayMode, label = "calendar-display") { mode ->
                    MonthGridView(
                        grid = grid,
                        selectedDate = selectedDate,
                        displayMode = mode,
                        eventDates = eventDates.keys,
                        onDayClick = { date ->
                            selectedDate = date
                            val targetMonth = YearMonth.from(date)
                            if (targetMonth != currentMonth) {
                                val offset = ChronoUnit.MONTHS.between(baseMonth, targetMonth).toInt()
                                scope.launch { pagerState.animateScrollToPage(INITIAL_PAGE + offset) }
                            }
                            if (date in eventDates.keys || Holidays.on(date).isNotEmpty()) {
                                displayMode = CalendarDisplayMode.WEEK_AGENDA
                            }
                        },
                    )
                }
            }
            AgendaHandle(
                displayMode = displayMode,
                onToggle = {
                    displayMode = if (displayMode == CalendarDisplayMode.MONTH) {
                        CalendarDisplayMode.WEEK_AGENDA
                    } else {
                        CalendarDisplayMode.MONTH
                    }
                },
            )
            SelectedDayAgenda(
                date = selectedDate,
                lunarDay = selectedLunar.day,
                lunarMonth = selectedLunar.month,
                leap = selectedLunar.isLeapMonth,
                yearCanChi = selectedYearCanChi.display,
                holidays = selectedHolidays,
                events = selectedEvents,
                permissionGranted = permission.granted,
                hasWritable = hasWritable,
                onRequestPermission = if (permission.deniedOnce) permission.openSettings else permission.request,
                onOpenDetail = { onOpenDayDetail(selectedDate) },
                onAddEvent = { onAddEvent(selectedDate) },
                onEditEvent = { eventId -> onEditEvent(eventId, selectedDate) },
                modifier = Modifier.weight(1f, fill = true),
            )
        }
    }

    if (showCalendarPicker) {
        CalendarPickerSheet(
            calendars = calendars,
            hiddenIds = hiddenIds,
            onToggle = viewModel::toggleCalendar,
            onDismiss = { showCalendarPicker = false },
        )
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    lunarYearLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tháng ${month.monthValue} ${month.year}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Năm $lunarYearLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        WEEKDAY_LABELS.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (label == "CN") {
                    LocalExtendedColors.current.holidayRed
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PermissionBanner(
    deniedOnce: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Kết nối Google Lịch để xem sự kiện.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = if (deniedOnce) onOpenSettings else onRequest) {
                Text(if (deniedOnce) "Cài đặt" else "Cho phép")
            }
        }
    }
}

@Composable
private fun AgendaHandle(displayMode: CalendarDisplayMode, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(top = 4.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(if (displayMode == CalendarDisplayMode.MONTH) 46.dp else 34.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun SelectedDayAgenda(
    date: LocalDate,
    lunarDay: Int,
    lunarMonth: Int,
    leap: Boolean,
    yearCanChi: String,
    holidays: List<Holiday>,
    events: List<CalendarEvent>,
    permissionGranted: Boolean,
    hasWritable: Boolean,
    onRequestPermission: () -> Unit,
    onOpenDetail: () -> Unit,
    onAddEvent: () -> Unit,
    onEditEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val leapLabel = if (leap) " nhuận" else ""
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${date.dayOfMonth}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Thg ${date.monthValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = date.dayOfWeek.vnLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Ngày $lunarDay tháng $lunarMonth$leapLabel ÂL · $yearCanChi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = onOpenDetail) {
                    Text("Chi tiết")
                }
            }
            if (holidays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    holidays.take(2).forEach { holiday ->
                        HolidayChip(holiday)
                    }
                }
            }
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                RowDivider()
                events.take(4).forEachIndexed { index, event ->
                    EventListRow(event = event, onClick = { onEditEvent(event.eventId) })
                    if (index != events.take(4).lastIndex) RowDivider(modifier = Modifier.padding(start = 21.dp))
                }
                if (events.size > 4) {
                    Text(
                        text = "+${events.size - 4} sự kiện khác",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    )
                }
            } else if (holidays.isEmpty()) {
                Text(
                    text = "Không có sự kiện",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            when {
                !permissionGranted -> TextButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.padding(start = 6.dp),
                ) {
                    Text("Cho phép truy cập lịch")
                }
                hasWritable -> TextButton(
                    onClick = onAddEvent,
                    modifier = Modifier.padding(start = 6.dp),
                ) {
                    Text("+ Thêm sự kiện")
                }
            }
        }
    }
}

@Composable
private fun HolidayChip(holiday: Holiday) {
    val color = if (holiday.type == HolidayType.PUBLIC) {
        LocalExtendedColors.current.holidayRed
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.13f),
    ) {
        Text(
            text = holiday.name,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun BottomCalendarBar(
    showToday: Boolean,
    onToday: () -> Unit,
    onCalendarPicker: (() -> Unit)?,
    onSettings: () -> Unit,
    onAddEvent: () -> Unit,
    canAddEvent: Boolean,
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onCalendarPicker != null) {
                IconButton(onClick = onCalendarPicker) {
                    Icon(Icons.Default.DateRange, contentDescription = "Lịch hiển thị")
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Lịch",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (showToday) {
                        Text(
                            text = " · Hôm nay",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(onClick = onToday),
                        )
                    }
                }
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
            }
            if (canAddEvent) {
                FilledIconButton(onClick = onAddEvent) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm sự kiện")
                }
            }
        }
    }
}

private val DayOfWeek.vnLabel: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "Thứ Hai"
        DayOfWeek.TUESDAY -> "Thứ Ba"
        DayOfWeek.WEDNESDAY -> "Thứ Tư"
        DayOfWeek.THURSDAY -> "Thứ Năm"
        DayOfWeek.FRIDAY -> "Thứ Sáu"
        DayOfWeek.SATURDAY -> "Thứ Bảy"
        DayOfWeek.SUNDAY -> "Chủ Nhật"
    }
