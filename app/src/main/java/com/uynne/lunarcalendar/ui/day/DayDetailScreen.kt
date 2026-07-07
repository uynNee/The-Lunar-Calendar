package com.uynne.lunarcalendar.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.data.Holiday
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.data.Holidays
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.lunar.MoonPhase
import com.uynne.lunarcalendar.ui.components.EventListRow
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
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

private val MoonPhase.glyph: String
    get() = when (this) {
        MoonPhase.NEW_MOON -> "🌑"
        MoonPhase.WAXING_CRESCENT -> "🌒"
        MoonPhase.FIRST_QUARTER -> "🌓"
        MoonPhase.WAXING_GIBBOUS -> "🌔"
        MoonPhase.FULL_MOON -> "🌕"
        MoonPhase.WANING_GIBBOUS -> "🌖"
        MoonPhase.LAST_QUARTER -> "🌗"
        MoonPhase.WANING_CRESCENT -> "🌘"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    date: LocalDate,
    onBack: () -> Unit,
    onAddEvent: (LocalDate) -> Unit = {},
    onEditEvent: (Long) -> Unit = {},
    eventsViewModel: DayEventsViewModel = viewModel(factory = DayEventsViewModel.Factory),
) {
    val permission = rememberCalendarPermissionState()
    val events by eventsViewModel.events.collectAsStateWithLifecycle()
    val hasWritable by eventsViewModel.hasWritableCalendar.collectAsStateWithLifecycle()
    LaunchedEffect(date) { eventsViewModel.setDate(date) }
    LaunchedEffect(permission.granted) { eventsViewModel.setPermissionGranted(permission.granted) }

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
    val leap = if (lunar.isLeapMonth) " nhuận" else ""
    val quality = if (star.quality == DayQuality.HOANG_DAO) "hoàng đạo" else "hắc đạo"
    val qualityColor = if (star.quality == DayQuality.HOANG_DAO) {
        LocalExtendedColors.current.hoangDao
    } else {
        LocalExtendedColors.current.hacDao
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết ngày", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LunarSummaryHero(
                date = date,
                lunarLabel = "Ngày ${lunar.day} tháng ${lunar.month}$leap năm ${yearCanChi.display}",
                canChi = dayCanChi.display,
                phase = phase,
                quality = quality,
                qualityColor = qualityColor,
            )

            GroupedSection(title = "Can Chi") {
                GroupedRow("Năm", yearCanChi.display, supportingText = "Can chi năm âm")
                RowDivider()
                GroupedRow(
                    label = "Tháng",
                    value = monthCanChi.display + if (lunar.isLeapMonth) " (nhuận)" else "",
                    supportingText = "Tháng ${lunar.month}$leap",
                )
                RowDivider()
                GroupedRow("Ngày", dayCanChi.display, supportingText = "Theo lịch âm Việt Nam")
            }

            GroupedSection(title = "Trăng và sao") {
                GroupedRow(
                    label = "Mặt trăng",
                    value = phase.vn,
                    supportingText = "Pha trăng hiện tại",
                    leading = { MoonBadge(phase) },
                )
                RowDivider()
                GroupedRow(
                    label = "Sao",
                    value = star.name,
                    valueColor = qualityColor,
                    supportingText = quality,
                    leading = { QualityDot(qualityColor) },
                )
            }

            GroupedSection(title = "Sự kiện") {
                if (holidays.isEmpty() && events.isEmpty()) {
                    Text(
                        text = "Không có sự kiện",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                holidays.forEachIndexed { index, holiday ->
                    HolidayDetailRow(holiday)
                    if (index != holidays.lastIndex || events.isNotEmpty()) RowDivider()
                }
                events.forEachIndexed { index, event ->
                    EventListRow(event = event, onClick = { onEditEvent(event.eventId) })
                    if (index != events.lastIndex) RowDivider(modifier = Modifier.padding(start = 21.dp))
                }
                when {
                    !permission.granted -> {
                        RowDivider()
                        TextButton(
                            onClick = if (permission.deniedOnce) permission.openSettings else permission.request,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                if (permission.deniedOnce) {
                                    "Mở cài đặt để cho phép truy cập lịch"
                                } else {
                                    "Cho phép truy cập lịch"
                                },
                            )
                        }
                    }
                    hasWritable -> {
                        RowDivider()
                        TextButton(
                            onClick = { onAddEvent(date) },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text("+ Thêm sự kiện")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LunarSummaryHero(
    date: LocalDate,
    lunarLabel: String,
    canChi: String,
    phase: MoonPhase,
    quality: String,
    qualityColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date.dayOfWeek.vn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${date.dayOfMonth}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "/${date.monthValue}/${date.year}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 3.dp, bottom = 5.dp),
                    )
                }
                Text(
                    text = lunarLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Ngày $canChi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MoonBadge(phase, large = true)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = qualityColor.copy(alpha = 0.14f),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = quality,
                        style = MaterialTheme.typography.labelMedium,
                        color = qualityColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MoonBadge(phase: MoonPhase, large: Boolean = false) {
    Surface(
        shape = CircleShape,
        color = LocalExtendedColors.current.moonAccent.copy(alpha = 0.16f),
        modifier = Modifier.size(if (large) 58.dp else 34.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = phase.glyph,
                style = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun QualityDot(color: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun HolidayDetailRow(holiday: Holiday) {
    val color = if (holiday.type == HolidayType.PUBLIC) {
        LocalExtendedColors.current.holidayRed
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    GroupedRow(
        label = holiday.name,
        value = if (holiday.type == HolidayType.PUBLIC) "Lễ chính" else "Truyền thống",
        valueColor = color,
        supportingText = "Ngày lễ",
        leading = { QualityDot(color) },
    )
}

@Preview(showBackground = true)
@Composable
private fun DayDetailPreview() {
    LunarCalendarTheme {
        DayDetailScreen(date = LocalDate.of(2025, 1, 29), onBack = {})
    }
}
