package com.uynne.lunarcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.LunarCalendar
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LunarCalendarTheme {
                LunarCalendarApp()
            }
        }
    }
}

@Composable
fun LunarCalendarApp(today: LocalDate = LocalDate.now()) {
    val lunar = remember(today) { LunarCalendar.solarToLunar(today) }
    val dayCanChi = remember(today) {
        LunarCalendar.canChiOfDay(today.dayOfMonth, today.monthValue, today.year)
    }
    val monthCanChi = remember(lunar) { LunarCalendar.canChiOfMonth(lunar.month, lunar.year) }
    val yearCanChi = remember(lunar) { LunarCalendar.canChiOfYear(lunar.year) }
    val phase = remember(today) {
        LunarCalendar.moonPhase(today.dayOfMonth, today.monthValue, today.year)
    }
    val quality = remember(today) {
        LunarCalendar.dayQuality(today.dayOfMonth, today.monthValue, today.year)
    }
    val star = remember(lunar, dayCanChi) { LunarCalendar.dayStar(lunar.month, dayCanChi.chi) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lich Am",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "%02d/%02d/%d".format(today.dayOfMonth, today.monthValue, today.year),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            val leapSuffix = if (lunar.isLeapMonth) " (nhuận)" else ""
            Text(
                text = "Ngày ${lunar.day} tháng ${lunar.month}$leapSuffix",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            InfoRow("Năm", yearCanChi.display)
            InfoRow("Tháng", monthCanChi.display)
            InfoRow("Ngày", dayCanChi.display)
            InfoRow("Trăng", phase.vn)
            InfoRow(
                "Sao",
                "${star.name} (${if (quality == DayQuality.HOANG_DAO) "hoàng đạo" else "hắc đạo"})"
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LunarCalendarAppPreview() {
    LunarCalendarTheme {
        LunarCalendarApp(today = LocalDate.of(2026, 7, 7))
    }
}
