package com.uynne.lunarcalendar.ui.month

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.uynne.lunarcalendar.calendar.DayCell
import com.uynne.lunarcalendar.calendar.MonthGrid
import com.uynne.lunarcalendar.calendar.lunarDayLabel
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalDate

/** How many collapsed rows the drag range spans (6 total rows, 1 stays visible as the agenda week). */
internal const val COLLAPSIBLE_ROWS = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthGridView(
    grid: MonthGrid,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    dragState: AnchoredDraggableState<CalendarDisplayMode>,
    onRowHeightMeasured: (Float) -> Unit,
    eventDates: Set<LocalDate> = emptySet(),
) {
    // -1 when selectedDate isn't in this page's month (e.g. an off-screen pager
    // neighbor) so every row on that page collapses uniformly instead of one
    // arbitrarily staying pinned open.
    val selectedWeekIndex = remember(grid, selectedDate) {
        grid.weeks.indexOfFirst { week -> week.any { it.date == selectedDate } }
    }
    val density = LocalDensity.current
    var fullRowHeight by remember { mutableStateOf(Dp.Unspecified) }
    val progress = dragProgress(dragState)

    Column(modifier = Modifier.fillMaxWidth()) {
        grid.weeks.forEachIndexed { index, week ->
            val collapse = if (index == selectedWeekIndex) 0f else progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        if (collapse == 0f) {
                            val heightDp = with(density) { size.height.toDp() }
                            if (fullRowHeight != heightDp) fullRowHeight = heightDp
                            onRowHeightMeasured(size.height.toFloat())
                        }
                    }
                    .then(
                        if (fullRowHeight.isSpecified) {
                            Modifier.height(fullRowHeight * (1f - collapse))
                        } else {
                            Modifier
                        },
                    )
                    .graphicsLayer {
                        alpha = 1f - collapse
                        transformOrigin = TransformOrigin(0.5f, if (index < selectedWeekIndex) 0f else 1f)
                        scaleY = 1f - collapse
                    },
            ) {
                week.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        DayCellView(
                            cell = cell,
                            selected = cell.date == selectedDate,
                            hasEvents = cell.date in eventDates,
                            onClick = { onDayClick(cell.date) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun dragProgress(dragState: AnchoredDraggableState<CalendarDisplayMode>): Float {
    val offset = dragState.offset
    if (offset.isNaN()) {
        return if (dragState.currentValue == CalendarDisplayMode.WEEK_AGENDA) 1f else 0f
    }
    val from = dragState.anchors.positionOf(CalendarDisplayMode.MONTH)
    val to = dragState.anchors.positionOf(CalendarDisplayMode.WEEK_AGENDA)
    val range = to - from
    return if (range == 0f) 0f else ((offset - from) / range).coerceIn(0f, 1f)
}

@Composable
private fun DayCellView(
    cell: DayCell,
    selected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
) {
    val extended = LocalExtendedColors.current
    val isSunday = cell.date.dayOfWeek == DayOfWeek.SUNDAY
    val hasPublicHoliday = cell.holidays.any { it.type == HolidayType.PUBLIC }
    val contentAlpha = if (cell.inCurrentMonth) 1f else 0.22f

    val solarColor = when {
        selected -> extended.selectedText
        hasPublicHoliday || isSunday -> extended.holidayRed
        else -> MaterialTheme.colorScheme.onBackground
    }.copy(alpha = if (selected) 1f else contentAlpha)

    val lunarColor = when {
        selected -> extended.selectedText
        cell.isLunarFirst || cell.isRam -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }.copy(alpha = if (selected) 0.9f else contentAlpha)

    Column(
        modifier = Modifier
            .aspectRatio(0.9f)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 3.dp)
            .background(
                color = if (selected) extended.selectedFill else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${cell.date.dayOfMonth}",
            style = MaterialTheme.typography.titleMedium,
            color = solarColor,
            fontWeight = if (selected || cell.isToday) FontWeight.SemiBold else FontWeight.Medium,
        )
        Text(
            text = lunarDayLabel(cell.lunar),
            style = MaterialTheme.typography.labelSmall,
            color = lunarColor,
            fontWeight = if (cell.isLunarFirst || cell.isRam) FontWeight.SemiBold else FontWeight.Normal,
        )
        Row(
            modifier = Modifier.height(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cell.holidays.isNotEmpty()) {
                Dot(
                    color = (if (hasPublicHoliday) extended.holidayRed else MaterialTheme.colorScheme.tertiary)
                        .copy(alpha = contentAlpha),
                )
            }
            if (hasEvents) {
                Dot(color = extended.eventDot.copy(alpha = contentAlpha))
            }
            if (cell.holidays.isEmpty() && !hasEvents) {
                Spacer(modifier = Modifier.size(4.dp))
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(4.dp)
            .background(color = color, shape = CircleShape),
    )
}
