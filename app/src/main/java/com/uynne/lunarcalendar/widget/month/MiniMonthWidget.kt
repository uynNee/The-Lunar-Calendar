package com.uynne.lunarcalendar.widget.month

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.uynne.lunarcalendar.MainActivity
import com.uynne.lunarcalendar.calendar.DayCell
import com.uynne.lunarcalendar.calendar.lunarDayLabel
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME
import com.uynne.lunarcalendar.widget.WidgetRefresh
import com.uynne.lunarcalendar.widget.WidgetSnapshot
import com.uynne.lunarcalendar.widget.WidgetTheme
import com.uynne.lunarcalendar.widget.buildWidgetSnapshot

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

class MiniMonthWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = buildWidgetSnapshot()
        provideContent {
            val theme = currentState(KEY_WIDGET_THEME)
            GlanceTheme(colors = WidgetTheme.fromPref(theme)) {
                MiniMonthContent(snapshot)
            }
        }
    }
}

@Composable
private fun MiniMonthContent(snapshot: WidgetSnapshot) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(16.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Text(
            text = "Tháng ${snapshot.today.monthValue} ${snapshot.today.year} · Năm ${snapshot.yearCanChi.display}",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.primary,
            ),
            modifier = GlanceModifier.padding(bottom = 2.dp),
        )
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            WEEKDAY_LABELS.forEach { label ->
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        color = if (label == "CN") GlanceTheme.colors.error else GlanceTheme.colors.onSurfaceVariant,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
        }
        snapshot.monthGrid.weeks.forEach { week ->
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                week.forEach { cell ->
                    MiniDayCell(cell)
                }
            }
        }
    }
}

@Composable
private fun androidx.glance.layout.RowScope.MiniDayCell(cell: DayCell) {
    val solarColor = when {
        cell.isToday -> GlanceTheme.colors.onPrimary
        !cell.inCurrentMonth -> GlanceTheme.colors.onSurfaceVariant
        cell.holidays.isNotEmpty() -> GlanceTheme.colors.error
        else -> GlanceTheme.colors.onBackground
    }
    val lunarColor = when {
        cell.isToday -> GlanceTheme.colors.onPrimary
        cell.isLunarFirst || cell.isRam -> GlanceTheme.colors.primary
        else -> GlanceTheme.colors.onSurfaceVariant
    }
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .padding(1.dp)
            .then(
                if (cell.isToday) {
                    GlanceModifier.background(GlanceTheme.colors.primary).cornerRadius(8.dp)
                } else {
                    GlanceModifier
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${cell.date.dayOfMonth}",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = solarColor,
            ),
        )
        Text(
            text = lunarDayLabel(cell.lunar),
            style = TextStyle(
                fontSize = 7.sp,
                textAlign = TextAlign.Center,
                color = lunarColor,
            ),
        )
    }
}

class MiniMonthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MiniMonthWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefresh.syncSchedules(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefresh.syncSchedules(context)
    }
}
