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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.uynne.lunarcalendar.MainActivity
import com.uynne.lunarcalendar.data.HolidayType
import com.uynne.lunarcalendar.lunar.DayQuality
import com.uynne.lunarcalendar.lunar.MoonPhase
import com.uynne.lunarcalendar.widget.KEY_WIDGET_ACCENT_COLOR
import com.uynne.lunarcalendar.widget.KEY_WIDGET_STYLE
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME_MODE
import com.uynne.lunarcalendar.widget.WidgetAccentColor
import com.uynne.lunarcalendar.widget.WidgetRefresh
import com.uynne.lunarcalendar.widget.WidgetSnapshot
import com.uynne.lunarcalendar.widget.WidgetStyle
import com.uynne.lunarcalendar.widget.WidgetTheme
import com.uynne.lunarcalendar.widget.WidgetThemeMode
import com.uynne.lunarcalendar.widget.appAppearanceForWidgets
import com.uynne.lunarcalendar.widget.buildWidgetSnapshot
import com.uynne.lunarcalendar.widget.vn

val EPOCH_DAY_PARAM = ActionParameters.Key<Long>(MainActivity.EXTRA_EPOCH_DAY)

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
        val appAppearance = appAppearanceForWidgets(context)
        provideContent {
            val themePref = currentState(KEY_WIDGET_THEME_MODE) ?: currentState(KEY_WIDGET_THEME)
            val themeMode = WidgetThemeMode.fromStoredValue(themePref)
            val style = WidgetStyle.fromStoredValue(currentState(KEY_WIDGET_STYLE))
            val accent = WidgetAccentColor.fromStoredValue(currentState(KEY_WIDGET_ACCENT_COLOR))
            GlanceTheme(colors = WidgetTheme.fromMode(themeMode, appAppearance)) {
                TodayLunarContent(snapshot, style, accent)
            }
        }
    }
}

@Composable
private fun TodayLunarContent(
    snapshot: WidgetSnapshot,
    style: WidgetStyle,
    accent: WidgetAccentColor,
) {
    val leap = if (snapshot.lunar.isLeapMonth) " nhuận" else ""
    val qualityLabel = if (snapshot.dayQuality == DayQuality.HOANG_DAO) "Hoàng đạo" else "Hắc đạo"
    val holiday = snapshot.holidays.firstOrNull { it.type == HolidayType.PUBLIC }
        ?: snapshot.holidays.firstOrNull()
    val lunarLine = "Ngày ${snapshot.lunar.day} tháng ${snapshot.lunar.month}$leap ÂL"
    val showLunarLine = style != WidgetStyle.MINIMAL
    val showCanChi = style == WidgetStyle.CALENDAR || style == WidgetStyle.COMBINED
    val showMoonLine = style == WidgetStyle.MOON || style == WidgetStyle.COMBINED
    val emphasizeLunar = style == WidgetStyle.LUNAR
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(15.dp)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(EPOCH_DAY_PARAM to snapshot.today.toEpochDay()),
                ),
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (emphasizeLunar) "${snapshot.lunar.day}" else "${snapshot.today.dayOfMonth}",
                style = TextStyle(
                    fontSize = if (style == WidgetStyle.MINIMAL) 38.sp else 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent.glanceColor(),
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(modifier = GlanceModifier.width(9.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = snapshot.today.dayOfWeek.vn,
                    style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
                Text(
                    text = if (emphasizeLunar) {
                        "${snapshot.today.dayOfMonth}/${snapshot.today.monthValue}/${snapshot.today.year}"
                    } else if (showLunarLine) {
                        lunarLine
                    } else {
                        ""
                    },
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
            }
        }
        if (emphasizeLunar) {
            Text(
                text = "$lunarLine · ${snapshot.dayCanChi.display}",
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.padding(top = 5.dp),
            )
        } else if (showCanChi) {
            Text(
                text = "Ngày ${snapshot.dayCanChi.display} · Năm ${snapshot.yearCanChi.display}",
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.padding(top = 5.dp),
            )
        }
        if (showMoonLine) {
            Text(
                text = "${snapshot.moonPhase.glyph} ${snapshot.moonPhase.vn}",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.padding(top = 5.dp),
            )
        }
        if (style == WidgetStyle.COMBINED) {
            Text(
                text = holiday?.let { "$qualityLabel · ${it.name}" } ?: qualityLabel,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (snapshot.dayQuality == DayQuality.HOANG_DAO) {
                        accent.glanceColor()
                    } else {
                        GlanceTheme.colors.onSurfaceVariant
                    },
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
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
