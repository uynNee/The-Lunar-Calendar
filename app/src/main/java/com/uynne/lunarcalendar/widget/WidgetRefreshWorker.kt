package com.uynne.lunarcalendar.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Periodic backstop: refreshes widgets and re-arms the midnight alarm. */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        WidgetRefresh.updateAllWidgets(applicationContext)
        WidgetRefresh.scheduleMidnightAlarm(applicationContext)
        return Result.success()
    }
}
