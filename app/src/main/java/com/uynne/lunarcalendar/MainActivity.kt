package com.uynne.lunarcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.uynne.lunarcalendar.ui.LunarCalendarApp
import com.uynne.lunarcalendar.ui.settings.AppearancePrefs
import com.uynne.lunarcalendar.ui.theme.AppearanceMode
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import com.uynne.lunarcalendar.widget.WidgetRefresh

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WidgetRefresh.syncSchedules(this)
        val epochDay = intent.getLongExtra(EXTRA_EPOCH_DAY, -1L).takeIf { it >= 0 }
        setContent {
            var appearanceMode by remember { mutableStateOf(AppearancePrefs.get(this)) }
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (appearanceMode) {
                AppearanceMode.SYSTEM -> systemDark
                AppearanceMode.LIGHT -> false
                AppearanceMode.DARK -> true
            }
            LunarCalendarTheme(darkTheme = darkTheme) {
                LunarCalendarApp(
                    initialEpochDay = epochDay,
                    appearanceMode = appearanceMode,
                    onAppearanceModeChange = { mode ->
                        AppearancePrefs.set(this, mode)
                        appearanceMode = mode
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_EPOCH_DAY = "epochDay"
    }
}
