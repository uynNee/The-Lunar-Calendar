package com.uynne.lunarcalendar.ui.month

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uynne.lunarcalendar.calendar.DayCell
import com.uynne.lunarcalendar.calendar.MonthGrid
import com.uynne.lunarcalendar.calendar.lunarDayLabel
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun MonthGridView(
    grid: MonthGrid,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    eventDates: Set<LocalDate> = emptySet(),
    displayMode: CalendarDisplayMode = CalendarDisplayMode.MONTH,
) {
    val weeks = when (displayMode) {
        CalendarDisplayMode.MONTH -> grid.weeks
        CalendarDisplayMode.WEEK_AGENDA -> grid.weeks.filter { week ->
            week.any { it.date == selectedDate }
        }.ifEmpty { grid.weeks.take(1) }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        DayCellView(
                            cell = cell,
                            selected = cell.date == selectedDate,
                            compact = displayMode == CalendarDisplayMode.WEEK_AGENDA,
                            hasEvents = cell.date in eventDates,
                            onClick = { onDayClick(cell.date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCellView(
    cell: DayCell,
    selected: Boolean,
    compact: Boolean,
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
            .aspectRatio(if (compact) 1.18f else 0.9f)
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
