package com.uynne.lunarcalendar.widget.moon

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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.uynne.lunarcalendar.MainActivity
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

class MoonPhaseWidget : GlanceAppWidget() {

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
                MoonPhaseContent(snapshot, style, accent)
            }
        }
    }
}

@Composable
private fun MoonPhaseContent(
    snapshot: WidgetSnapshot,
    style: WidgetStyle,
    accent: WidgetAccentColor,
) {
    val showPhaseName = style != WidgetStyle.MINIMAL
    val showDate = style == WidgetStyle.CALENDAR || style == WidgetStyle.COMBINED
    val showLunarDate = style == WidgetStyle.LUNAR || style == WidgetStyle.COMBINED
    val showCountdowns = style == WidgetStyle.MOON || style == WidgetStyle.COMBINED
    val leap = if (snapshot.lunar.isLeapMonth) " nhuận" else ""
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = snapshot.moonPhase.glyph,
            style = TextStyle(fontSize = if (style == WidgetStyle.MINIMAL) 34.sp else 40.sp, textAlign = TextAlign.Center),
        )
        if (showPhaseName) {
            Text(
                text = snapshot.moonPhase.vn,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = GlanceTheme.colors.onSurface,
                ),
                maxLines = 1,
            )
        }
        if (showDate) {
            Text(
                text = "${snapshot.today.dayOfMonth}/${snapshot.today.monthValue}/${snapshot.today.year}",
                style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center, color = GlanceTheme.colors.onSurfaceVariant),
            )
        }
        if (showLunarDate) {
            Text(
                text = "Ngày ${snapshot.lunar.day} tháng ${snapshot.lunar.month}$leap ÂL",
                style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center, color = accent.glanceColor()),
            )
        }
        if (showCountdowns) {
            Text(
                text = if (snapshot.daysToRam == 0) {
                    "Rằm hôm nay"
                } else {
                    "Rằm: ${snapshot.daysToRam} ngày"
                },
                style = TextStyle(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = accent.glanceColor(),
                ),
            )
            Text(
                text = if (snapshot.daysToMung1 == 0) {
                    "Mùng 1 hôm nay"
                } else {
                    "Mùng 1: ${snapshot.daysToMung1} ngày"
                },
                style = TextStyle(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
            )
        }
    }
}

class MoonPhaseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoonPhaseWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefresh.syncSchedules(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefresh.syncSchedules(context)
    }
}
