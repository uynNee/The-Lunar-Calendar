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
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME
import com.uynne.lunarcalendar.widget.WidgetRefresh
import com.uynne.lunarcalendar.widget.WidgetSnapshot
import com.uynne.lunarcalendar.widget.WidgetTheme
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
        provideContent {
            val theme = currentState(KEY_WIDGET_THEME)
            GlanceTheme(colors = WidgetTheme.fromPref(theme)) {
                MoonPhaseContent(snapshot)
            }
        }
    }
}

@Composable
private fun MoonPhaseContent(snapshot: WidgetSnapshot) {
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
            style = TextStyle(fontSize = 38.sp, textAlign = TextAlign.Center),
        )
        Text(
            text = snapshot.moonPhase.vn,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = GlanceTheme.colors.onSurface,
            ),
        )
        Text(
            text = if (snapshot.daysToRam == 0) {
                "Rằm hôm nay"
            } else {
                "Rằm: ${snapshot.daysToRam} ngày"
            },
            style = TextStyle(
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = GlanceTheme.colors.primary,
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
