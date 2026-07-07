package com.uynne.lunarcalendar.ui.month

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uynne.lunarcalendar.calendar.buildMonthGrid
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 12_000
private const val INITIAL_PAGE = PAGE_COUNT / 2

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(today: LocalDate, onDayClick: (LocalDate) -> Unit) {
    val baseMonth = remember(today) { YearMonth.from(today) }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { PAGE_COUNT }
    val scope = rememberCoroutineScope()
    val currentMonth = baseMonth.plusMonths((pagerState.currentPage - INITIAL_PAGE).toLong())
    // Lunar year shown for the month's mid-point (Tết can split a solar month).
    val lunarYear = remember(currentMonth) {
        LunarCalendar.solarToLunar(currentMonth.atDay(15)).year
    }

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
                MonthGridView(grid = grid, onDayClick = onDayClick)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthScreenPreview() {
    LunarCalendarTheme {
        MonthScreen(today = LocalDate.of(2026, 7, 7), onDayClick = {})
    }
}
