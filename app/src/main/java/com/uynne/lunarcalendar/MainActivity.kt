package com.uynne.lunarcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uynne.lunarcalendar.ui.LunarCalendarApp
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import com.uynne.lunarcalendar.widget.WidgetRefresh

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WidgetRefresh.syncSchedules(this)
        val epochDay = intent.getLongExtra(EXTRA_EPOCH_DAY, -1L).takeIf { it >= 0 }
        setContent {
            LunarCalendarTheme {
                LunarCalendarApp(initialEpochDay = epochDay)
            }
        }
    }

    companion object {
        const val EXTRA_EPOCH_DAY = "epochDay"
    }
}
