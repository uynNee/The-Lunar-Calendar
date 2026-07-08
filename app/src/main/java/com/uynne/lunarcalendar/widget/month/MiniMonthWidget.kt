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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.uynne.lunarcalendar.MainActivity
import com.uynne.lunarcalendar.calendar.DayCell
import com.uynne.lunarcalendar.calendar.lunarDayLabel
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

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

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

class MiniMonthWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = buildWidgetSnapshot()
        val appAppearance = appAppearanceForWidgets(context)
        provideContent {
            val themePref = currentState(KEY_WIDGET_THEME_MODE) ?: currentState(KEY_WIDGET_THEME)
            val themeMode = WidgetThemeMode.fromStoredValue(themePref)
            val style = WidgetStyle.fromStoredValue(currentState(KEY_WIDGET_STYLE))
            val accent = WidgetAccentColor.fromStoredValue(currentState(KEY_WIDGET_ACCENT_COLOR))
            GlanceTheme(colors = WidgetTheme.fromMode(themeMode, appAppearance)) {
                MiniMonthContent(snapshot, style, accent)
            }
        }
    }
}

@Composable
private fun MiniMonthContent(
    snapshot: WidgetSnapshot,
    style: WidgetStyle,
    accent: WidgetAccentColor,
) {
    val showYearLabel = style != WidgetStyle.MINIMAL
    val showLunarLabels = style == WidgetStyle.LUNAR || style == WidgetStyle.COMBINED
    val showMoonBadge = style == WidgetStyle.MOON || style == WidgetStyle.COMBINED
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "Tháng ${snapshot.today.monthValue} ${snapshot.today.year}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (showMoonBadge) {
                Text(
                    text = snapshot.moonPhase.glyph,
                    style = TextStyle(fontSize = 14.sp),
                )
            }
        }
        if (showYearLabel) {
            Text(
                text = "Năm ${snapshot.yearCanChi.display}",
                style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.padding(bottom = 3.dp),
            )
        } else {
            Spacer(modifier = GlanceModifier.height(3.dp))
        }
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
                    MiniDayCell(cell, showLunarLabels, accent)
                }
            }
        }
    }
}

@Composable
private fun androidx.glance.layout.RowScope.MiniDayCell(
    cell: DayCell,
    showLunarLabel: Boolean,
    accent: WidgetAccentColor,
) {
    val solarColor = when {
        cell.isToday -> GlanceTheme.colors.onPrimary
        !cell.inCurrentMonth -> GlanceTheme.colors.onSurfaceVariant
        cell.holidays.isNotEmpty() -> GlanceTheme.colors.error
        else -> GlanceTheme.colors.onBackground
    }
    val lunarColor = when {
        cell.isToday -> GlanceTheme.colors.onPrimary
        cell.isLunarFirst || cell.isRam -> accent.glanceColor()
        else -> GlanceTheme.colors.onSurfaceVariant
    }
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .padding(1.dp)
            .then(
                if (cell.isToday) {
                    GlanceModifier.background(accent.glanceColor()).cornerRadius(11.dp)
                } else {
                    GlanceModifier
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${cell.date.dayOfMonth}",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = solarColor,
            ),
        )
        if (showLunarLabel) {
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
