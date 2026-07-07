package com.uynne.lunarcalendar.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun MonthGridView(grid: MonthGrid, onDayClick: (LocalDate) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        grid.weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        DayCellView(cell = cell, onClick = { onDayClick(cell.date) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, onClick: () -> Unit) {
    val extended = LocalExtendedColors.current
    val isSunday = cell.date.dayOfWeek == DayOfWeek.SUNDAY
    val hasPublicHoliday = cell.holidays.any { it.type == HolidayType.PUBLIC }
    val contentAlpha = if (cell.inCurrentMonth) 1f else 0.38f

    val solarColor = when {
        cell.isToday -> MaterialTheme.colorScheme.onPrimary
        hasPublicHoliday -> extended.holidayRed
        isSunday -> extended.holidayRed
        else -> MaterialTheme.colorScheme.onBackground
    }.copy(alpha = if (cell.isToday) 1f else contentAlpha)

    val lunarColor = when {
        cell.isLunarFirst || cell.isRam -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }.copy(alpha = contentAlpha)

    Column(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (cell.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${cell.date.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                color = solarColor,
            )
        }
        Text(
            text = lunarDayLabel(cell.lunar),
            style = MaterialTheme.typography.labelSmall,
            color = lunarColor,
            fontWeight = if (cell.isLunarFirst || cell.isRam) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (cell.holidays.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(
                        color = (if (hasPublicHoliday) extended.holidayRed else MaterialTheme.colorScheme.tertiary)
                            .copy(alpha = contentAlpha),
                        shape = CircleShape,
                    ),
            )
        } else {
            Spacer(modifier = Modifier.padding(top = 2.dp).size(4.dp))
        }
    }
}
