package com.uynne.lunarcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uynne.lunarcalendar.data.calendar.CalendarEvent
import com.uynne.lunarcalendar.ui.theme.LocalExtendedColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun GroupedSection(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            SectionTitle(title)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 16.dp, bottom = 6.dp, top = 4.dp),
    )
}

@Composable
fun GroupedRow(
    label: String,
    value: String? = null,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = if (supportingText == null) 9.dp else 8.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(modifier = Modifier.padding(end = 12.dp)) {
                leading()
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(start = 10.dp)) {
                trailing()
            }
        }
    }
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 16.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(LocalExtendedColors.current.separator),
    )
}

@Composable
fun EventListRow(event: CalendarEvent, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val zone = ZoneId.systemDefault()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    val timeLabel = if (event.allDay) {
        "Cả ngày"
    } else {
        val begin = Instant.ofEpochMilli(event.beginMillis).atZone(zone).toLocalTime()
        val end = Instant.ofEpochMilli(event.endMillis).atZone(zone).toLocalTime()
        "${timeFormat.format(begin)} - ${timeFormat.format(end)}"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(event.color)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = if (event.isRecurring) "$timeLabel · Lặp lại" else timeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SmallColorDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}
