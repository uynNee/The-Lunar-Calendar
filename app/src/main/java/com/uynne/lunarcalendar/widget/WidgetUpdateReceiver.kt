package com.uynne.lunarcalendar.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Refreshes all widgets on midnight alarm, clock/timezone changes, boot, and app update. */
class WidgetUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WidgetRefresh.ACTION_MIDNIGHT,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> {
                val result = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        WidgetRefresh.updateAllWidgets(appContext)
                        WidgetRefresh.scheduleMidnightAlarm(appContext)
                    } finally {
                        result.finish()
                    }
                }
            }
        }
    }
}
