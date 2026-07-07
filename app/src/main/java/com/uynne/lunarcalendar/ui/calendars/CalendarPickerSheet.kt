package com.uynne.lunarcalendar.ui.calendars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uynne.lunarcalendar.data.calendar.DeviceCalendar
import com.uynne.lunarcalendar.ui.components.RowDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPickerSheet(
    calendars: List<DeviceCalendar>,
    hiddenIds: Set<Long>,
    onToggle: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 34.dp),
        ) {
            Text(
                text = "Hiển thị lịch",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            if (calendars.isEmpty()) {
                Text(
                    text = "Không tìm thấy lịch nào trên thiết bị.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            calendars.groupBy { it.accountName }.forEach { (account, group) ->
                Text(
                    text = account,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp, start = 4.dp),
                )
                group.forEachIndexed { index, calendar ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(calendar.color), CircleShape),
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        ) {
                            Text(
                                text = calendar.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            if (!calendar.isVisible) {
                                Text(
                                    text = "Ẩn trong Google Lịch",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Checkbox(
                            checked = calendar.isVisible && calendar.id !in hiddenIds,
                            onCheckedChange = { checked -> onToggle(calendar.id, !checked) },
                            enabled = calendar.isVisible,
                        )
                    }
                    if (index != group.lastIndex) RowDivider(modifier = Modifier.padding(start = 22.dp))
                }
            }
        }
    }
}
