package com.uynne.lunarcalendar.ui.month

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.calendar.buildMonthGrid
import com.uynne.lunarcalendar.data.Holiday
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.data.calendar.CalendarEvent
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.components.EventListRow
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
import com.uynne.lunarcalendar.ui.theme.Dimens
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 12_000
private const val INITIAL_PAGE = PAGE_COUNT / 2

private val WEEKDAY_SHORT_LABELS = mapOf(
    DayOfWeek.MONDAY to "T2",
    DayOfWeek.TUESDAY to "T3",
    DayOfWeek.WEDNESDAY to "T4",
    DayOfWeek.THURSDAY to "T5",
    DayOfWeek.FRIDAY to "T6",
    DayOfWeek.SATURDAY to "T7",
    DayOfWeek.SUNDAY to "CN",
)

private fun weekdayLabels(weekStart: DayOfWeek): List<String> =
    (0 until 7).map { WEEKDAY_SHORT_LABELS.getValue(weekStart.plus(it.toLong())) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val density = LocalDensity.current
    val currentMonth = baseMonth.plusMonths((pagerState.currentPage - INITIAL_PAGE).toLong())
    var selectedDate by remember(today) { mutableStateOf(today) }
    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    val dragState = remember {
        AnchoredDraggableState(
            initialValue = CalendarDisplayMode.MONTH,
            anchors = DraggableAnchors {
                CalendarDisplayMode.MONTH at 0f
                CalendarDisplayMode.WEEK_AGENDA at 0f
            },
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = tween(220),
            decayAnimationSpec = splineBasedDecay(density),
        )
    }
    LaunchedEffect(rowHeightPx) {
        if (rowHeightPx > 0f) {
            dragState.updateAnchors(
                DraggableAnchors {
                    CalendarDisplayMode.MONTH at 0f
                    CalendarDisplayMode.WEEK_AGENDA at rowHeightPx * COLLAPSIBLE_ROWS
                },
            )
        }
    }

    val lunarYear = remember(currentMonth) {
        LunarCalendar.solarToLunar(currentMonth.atDay(15)).year
    }
    val selectedLunar = remember(selectedDate) { LunarCalendar.solarToLunar(selectedDate) }
    val selectedHolidays = remember(selectedDate) { Holidays.on(selectedDate) }

    val permission = rememberCalendarPermissionState()
    val eventDates by viewModel.eventDates.collectAsStateWithLifecycle()
    val weekStart by viewModel.weekStart.collectAsStateWithLifecycle()
    val selectedEvents by dayEventsViewModel.events.collectAsStateWithLifecycle()
    val hasWritable by dayEventsViewModel.hasWritableCalendar.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

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

    fun jumpToDate(picked: LocalDate) {
        selectedDate = picked
        val targetMonth = YearMonth.from(picked)
        if (targetMonth != currentMonth) {
            val offset = ChronoUnit.MONTHS.between(baseMonth, targetMonth).toInt()
            scope.launch { pagerState.animateScrollToPage(INITIAL_PAGE + offset) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomCalendarBar(
                showToday = pagerState.currentPage != INITIAL_PAGE || selectedDate != today,
                onToday = {
                    selectedDate = today
                    scope.launch { dragState.animateTo(CalendarDisplayMode.MONTH) }
                    scope.launch { pagerState.animateScrollToPage(INITIAL_PAGE) }
                },
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
                .padding(horizontal = 16.dp),
        ) {
            MonthHeader(
                month = currentMonth,
                lunarYearLabel = LunarCalendar.canChiOfYear(lunarYear).display,
                onClick = { showDatePicker = true },
            )
            if (!permission.granted) {
                PermissionBanner(
                    deniedOnce = permission.deniedOnce,
                    onRequest = permission.request,
                    onOpenSettings = permission.openSettings,
                )
            }
            WeekdayHeader(weekStart)
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val month = baseMonth.plusMonths((page - INITIAL_PAGE).toLong())
                val grid = remember(page, today, weekStart) { buildMonthGrid(month, today, weekStart) }
                MonthGridView(
                    grid = grid,
                    selectedDate = selectedDate,
                    dragState = dragState,
                    onRowHeightMeasured = { height -> rowHeightPx = height },
                    eventDates = eventDates.keys,
                    onDayClick = { date -> jumpToDate(date) },
                )
            }
            AgendaHandle(dragState = dragState, scope = scope)
            SelectedDayAgenda(
                date = selectedDate,
                lunarDay = selectedLunar.day,
                lunarMonth = selectedLunar.month,
                leap = selectedLunar.isLeapMonth,
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

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        jumpToDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
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
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    lunarYearLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = Dimens.spaceSM, bottom = Dimens.spaceXS),
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
private fun WeekdayHeader(weekStart: DayOfWeek) {
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdayLabels(weekStart).forEach { label ->
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
                    .padding(vertical = Dimens.spaceXS),
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
            .padding(bottom = Dimens.spaceXS),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(Dimens.radiusMD),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spaceMD, vertical = Dimens.spaceSM),
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
@OptIn(ExperimentalFoundationApi::class)
private fun handleDragProgress(dragState: AnchoredDraggableState<CalendarDisplayMode>): Float {
    val offset = dragState.offset
    if (offset.isNaN()) {
        return if (dragState.currentValue == CalendarDisplayMode.WEEK_AGENDA) 1f else 0f
    }
    val from = dragState.anchors.positionOf(CalendarDisplayMode.MONTH)
    val to = dragState.anchors.positionOf(CalendarDisplayMode.WEEK_AGENDA)
    val range = to - from
    return if (range == 0f) 0f else ((offset - from) / range).coerceIn(0f, 1f)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AgendaHandle(dragState: AnchoredDraggableState<CalendarDisplayMode>, scope: CoroutineScope) {
    val progress = handleDragProgress(dragState)
    val pillWidth = lerp(46.dp, 34.dp, progress)
    val pillColor = lerpColor(
        MaterialTheme.colorScheme.outlineVariant,
        MaterialTheme.colorScheme.primary,
        progress,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .anchoredDraggable(state = dragState, reverseDirection = true, orientation = Orientation.Vertical)
            .clickable {
                scope.launch {
                    dragState.animateTo(
                        if (dragState.currentValue == CalendarDisplayMode.MONTH) {
                            CalendarDisplayMode.WEEK_AGENDA
                        } else {
                            CalendarDisplayMode.MONTH
                        },
                    )
                }
            }
            .padding(top = Dimens.spaceXXS, bottom = Dimens.spaceXS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(pillWidth)
                .height(4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(pillColor),
        )
    }
}

@Composable
private fun SelectedDayAgenda(
    date: LocalDate,
    lunarDay: Int,
    lunarMonth: Int,
    leap: Boolean,
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
            .padding(bottom = Dimens.spaceXS),
        shape = RoundedCornerShape(Dimens.radiusLG),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .padding(top = Dimens.spaceSM, bottom = Dimens.spaceXS)
                .animateContentSize(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Dimens.spaceMD),
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
                Spacer(modifier = Modifier.width(Dimens.spaceSM))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = date.dayOfWeek.vnLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Ngày $lunarDay tháng $lunarMonth$leapLabel ÂL",
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
                Spacer(modifier = Modifier.height(Dimens.spaceXS))
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.spaceMD),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXS),
                ) {
                    holidays.take(2).forEach { holiday ->
                        HolidayChip(holiday)
                    }
                }
            }
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.spaceXS))
                RowDivider()
                events.take(4).forEachIndexed { index, event ->
                    EventListRow(event = event, onClick = { onEditEvent(event.eventId) })
                    if (index != events.take(4).lastIndex) RowDivider(modifier = Modifier.padding(start = Dimens.spaceMD))
                }
                if (events.size > 4) {
                    Text(
                        text = "+${events.size - 4} sự kiện khác",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Dimens.spaceMD, vertical = Dimens.spaceXXS),
                    )
                }
            } else if (holidays.isEmpty()) {
                EmptyAgendaRow(
                    permissionGranted = permissionGranted,
                    hasWritable = hasWritable,
                    onRequestPermission = onRequestPermission,
                    onAddEvent = onAddEvent,
                )
            } else {
                AgendaAction(permissionGranted, hasWritable, onRequestPermission, onAddEvent)
            }
            if (events.isNotEmpty()) {
                AgendaAction(permissionGranted, hasWritable, onRequestPermission, onAddEvent)
            }
        }
    }
}

@Composable
private fun AgendaAction(
    permissionGranted: Boolean,
    hasWritable: Boolean,
    onRequestPermission: () -> Unit,
    onAddEvent: () -> Unit,
) {
    when {
        !permissionGranted -> TextButton(
            onClick = onRequestPermission,
            modifier = Modifier.padding(start = Dimens.spaceXXS),
        ) { Text("Cho phép truy cập lịch") }
        hasWritable -> TextButton(
            onClick = onAddEvent,
            modifier = Modifier.padding(start = Dimens.spaceXXS),
        ) { Text("+ Thêm sự kiện") }
    }
}

@Composable
private fun EmptyAgendaRow(
    permissionGranted: Boolean,
    hasWritable: Boolean,
    onRequestPermission: () -> Unit,
    onAddEvent: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spaceMD, vertical = Dimens.spaceXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.iconMD),
        )
        Spacer(modifier = Modifier.width(Dimens.spaceXS))
        Column {
            Text(
                text = "Không có sự kiện",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                !permissionGranted -> TextButton(
                    onClick = onRequestPermission,
                    contentPadding = PaddingValues(0.dp),
                ) { Text("Cho phép truy cập lịch") }
                hasWritable -> TextButton(
                    onClick = onAddEvent,
                    contentPadding = PaddingValues(0.dp),
                ) { Text("+ Thêm sự kiện") }
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
            modifier = Modifier.padding(horizontal = Dimens.spaceSM, vertical = Dimens.spaceXXS),
        )
    }
}

@Composable
private fun BottomCalendarBar(
    showToday: Boolean,
    onToday: () -> Unit,
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
                .padding(horizontal = Dimens.spaceMD, vertical = Dimens.spaceSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXS),
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.spaceSM, vertical = Dimens.spaceXS),
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
