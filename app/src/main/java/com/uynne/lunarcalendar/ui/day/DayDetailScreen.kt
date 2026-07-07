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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.LunarCalendar
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
fun DayDetailScreen(date: LocalDate, onBack: () -> Unit) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết ngày") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailCard {
                Text(
                    text = date.dayOfWeek.vn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year),
                    style = MaterialTheme.typography.displaySmall,
                )
                val leap = if (lunar.isLeapMonth) " nhuận" else ""
                Text(
                    text = "Ngày ${lunar.day} tháng ${lunar.month}$leap năm ${yearCanChi.display}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            DetailCard(title = "Can Chi") {
                InfoRow("Năm", yearCanChi.display)
                InfoRow("Tháng", monthCanChi.display + if (lunar.isLeapMonth) " (nhuận)" else "")
                InfoRow("Ngày", dayCanChi.display)
            }
            DetailCard(title = "Trăng và sao") {
                InfoRow("Trăng", phase.vn)
                val quality = if (star.quality == DayQuality.HOANG_DAO) "hoàng đạo" else "hắc đạo"
                val qualityColor = if (star.quality == DayQuality.HOANG_DAO) {
                    LocalExtendedColors.current.hoangDao
                } else {
                    LocalExtendedColors.current.hacDao
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sao",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${star.name} ($quality)",
                        color = qualityColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            DetailCard(title = "Sự kiện") {
                if (holidays.isEmpty()) {
                    Text(
                        text = "Không có sự kiện",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    holidays.forEach { holiday ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = holiday.name,
                                fontWeight = FontWeight.Medium,
                                color = if (holiday.type == HolidayType.PUBLIC) {
                                    LocalExtendedColors.current.holidayRed
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                },
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = if (holiday.type == HolidayType.PUBLIC) "Lễ chính" else "Truyền thống",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(title: String? = null, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun DayDetailPreview() {
    LunarCalendarTheme {
        DayDetailScreen(date = LocalDate.of(2025, 1, 29), onBack = {})
    }
}
