package com.uynne.lunarcalendar.widget.today

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.uynne.lunarcalendar.MainActivity
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME
import com.uynne.lunarcalendar.widget.WidgetRefresh
import com.uynne.lunarcalendar.widget.WidgetSnapshot
import com.uynne.lunarcalendar.widget.WidgetTheme
import com.uynne.lunarcalendar.widget.buildWidgetSnapshot
import com.uynne.lunarcalendar.widget.vn
import java.time.LocalDate

val EPOCH_DAY_PARAM = ActionParameters.Key<Long>(MainActivity.EXTRA_EPOCH_DAY)

class TodayLunarWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            androidx.compose.ui.unit.DpSize(180.dp, 60.dp),
            androidx.compose.ui.unit.DpSize(250.dp, 110.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = buildWidgetSnapshot()
        provideContent {
            val theme = currentState(KEY_WIDGET_THEME)
            GlanceTheme(colors = WidgetTheme.fromPref(theme)) {
                TodayLunarContent(snapshot)
            }
        }
    }
}

@Composable
private fun TodayLunarContent(snapshot: WidgetSnapshot) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(16.dp)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(EPOCH_DAY_PARAM to snapshot.today.toEpochDay()),
                ),
            ),
    ) {
        Row {
            Text(
                text = "${snapshot.today.dayOfMonth}",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                ),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column {
                Text(
                    text = snapshot.today.dayOfWeek.vn,
                    style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
                val leap = if (snapshot.lunar.isLeapMonth) " nhuận" else ""
                Text(
                    text = "Ngày ${snapshot.lunar.day} tháng ${snapshot.lunar.month}$leap ÂL",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
            }
        }
        Text(
            text = "Ngày ${snapshot.dayCanChi.display} · Năm ${snapshot.yearCanChi.display}",
            style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant),
        )
        val qualityLabel = if (snapshot.dayQuality == DayQuality.HOANG_DAO) "Hoàng đạo" else "Hắc đạo"
        val holiday = snapshot.holidays.firstOrNull { it.type == HolidayType.PUBLIC }
            ?: snapshot.holidays.firstOrNull()
        Text(
            text = holiday?.let { "$qualityLabel · ${it.name}" } ?: qualityLabel,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (snapshot.dayQuality == DayQuality.HOANG_DAO) {
                    GlanceTheme.colors.primary
                } else {
                    GlanceTheme.colors.onSurfaceVariant
                },
            ),
        )
    }
}

class TodayLunarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayLunarWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefresh.syncSchedules(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefresh.syncSchedules(context)
    }
}
