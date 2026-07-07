package com.uynne.lunarcalendar.ui.month

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.calendar.buildMonthGrid
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.calendars.CalendarPickerSheet
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 12_000
private const val INITIAL_PAGE = PAGE_COUNT / 2

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    viewModel: MonthViewModel = viewModel(factory = MonthViewModel.Factory),
) {
    val baseMonth = remember(today) { YearMonth.from(today) }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { PAGE_COUNT }
    val scope = rememberCoroutineScope()
    val currentMonth = baseMonth.plusMonths((pagerState.currentPage - INITIAL_PAGE).toLong())
    // Lunar year shown for the month's mid-point (Tết can split a solar month).
    val lunarYear = remember(currentMonth) {
        LunarCalendar.solarToLunar(currentMonth.atDay(15)).year
    }

    val permission = rememberCalendarPermissionState()
    val eventDates by viewModel.eventDates.collectAsStateWithLifecycle()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val hiddenIds by viewModel.hiddenIds.collectAsStateWithLifecycle()
    var showCalendarPicker by remember { mutableStateOf(false) }

    LaunchedEffect(permission.granted) { viewModel.setPermissionGranted(permission.granted) }
    LaunchedEffect(currentMonth) { viewModel.setVisibleMonth(currentMonth) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tháng ${currentMonth.monthValue} · ${currentMonth.year}")
                        Text(
                            text = "Năm ${LunarCalendar.canChiOfYear(lunarYear).display}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    if (pagerState.currentPage != INITIAL_PAGE) {
                        TextButton(onClick = {
                            scope.launch { pagerState.animateScrollToPage(INITIAL_PAGE) }
                        }) {
                            Text("Hôm nay")
                        }
                    }
                    if (permission.granted) {
                        IconButton(onClick = {
                            viewModel.refreshCalendars()
                            showCalendarPicker = true
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Hiển thị lịch")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
        ) {
            if (!permission.granted) {
                PermissionBanner(
                    deniedOnce = permission.deniedOnce,
                    onRequest = permission.request,
                    onOpenSettings = permission.openSettings,
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAY_LABELS.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (label == "CN") {
                            LocalExtendedColors.current.holidayRed
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.Top,
            ) { page ->
                val month = baseMonth.plusMonths((page - INITIAL_PAGE).toLong())
                val grid = remember(page, today) { buildMonthGrid(month, today) }
                MonthGridView(grid = grid, eventDates = eventDates.keys, onDayClick = onDayClick)
            }
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
private fun PermissionBanner(
    deniedOnce: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Kết nối Google Lịch để xem sự kiện của bạn",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row {
                if (deniedOnce) {
                    TextButton(onClick = onOpenSettings) { Text("Mở cài đặt") }
                } else {
                    TextButton(onClick = onRequest) { Text("Cho phép") }
                }
            }
        }
    }
}
