package com.uynne.lunarcalendar.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.uynne.lunarcalendar.widget.month.MiniMonthWidget
import com.uynne.lunarcalendar.widget.moon.MoonPhaseWidget
import com.uynne.lunarcalendar.widget.today.TodayLunarWidget
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetRefresh {

    const val ACTION_MIDNIGHT = "com.uynne.lunarcalendar.action.MIDNIGHT_UPDATE"
    private const val WORK_NAME = "widget_refresh"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend fun updateAllWidgets(context: Context) {
        TodayLunarWidget().updateAll(context)
        MiniMonthWidget().updateAll(context)
        MoonPhaseWidget().updateAll(context)
    }

    /** Pushes new defaults into every already-placed widget's own Glance state, then redraws them. */
    suspend fun pushDefaultsToAllWidgets(context: Context, defaults: WidgetDefaultsPrefs.Defaults) {
        val manager = GlanceAppWidgetManager(context)
        val idsByType = listOf(
            manager.getGlanceIds(TodayLunarWidget::class.java),
            manager.getGlanceIds(MiniMonthWidget::class.java),
            manager.getGlanceIds(MoonPhaseWidget::class.java),
        ).flatten()
        idsByType.forEach { glanceId ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_WIDGET_THEME_MODE] = defaults.themeMode.storedValue
                    this[KEY_WIDGET_THEME] = defaults.themeMode.storedValue
                    this[KEY_WIDGET_STYLE] = defaults.style.storedValue
                    this[KEY_WIDGET_ACCENT_COLOR] = defaults.accentColor.storedValue
                }
            }
        }
        updateAllWidgets(context)
    }

    /**
     * Inexact-but-doze-tolerant alarm shortly after local midnight; avoids the
     * Android 14 SCHEDULE_EXACT_ALARM permission. Re-armed on every fire and by
     * the periodic worker (self-heal on OEMs that drop alarms).
     */
    fun scheduleMidnightAlarm(context: Context) {
        val triggerAt = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .plusSeconds(5)
            .toInstant()
            .toEpochMilli()
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            midnightPendingIntent(context),
        )
    }

    fun cancelMidnightAlarm(context: Context) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(midnightPendingIntent(context))
    }

    fun schedulePeriodicWork(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetRefreshWorker>(6, TimeUnit.HOURS).build(),
        )
    }

    fun cancelPeriodicWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    suspend fun hasAnyWidget(context: Context): Boolean {
        val manager = GlanceAppWidgetManager(context)
        return manager.getGlanceIds(TodayLunarWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(MiniMonthWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(MoonPhaseWidget::class.java).isNotEmpty()
    }

    /** Arms alarm + periodic work while at least one widget exists, cancels both otherwise. */
    fun syncSchedules(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            if (hasAnyWidget(appContext)) {
                scheduleMidnightAlarm(appContext)
                schedulePeriodicWork(appContext)
            } else {
                cancelMidnightAlarm(appContext)
                cancelPeriodicWork(appContext)
            }
        }
    }

    private fun midnightPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, WidgetUpdateReceiver::class.java).setAction(ACTION_MIDNIGHT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
