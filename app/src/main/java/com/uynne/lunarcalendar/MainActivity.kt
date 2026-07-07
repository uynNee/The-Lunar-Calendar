package com.uynne.lunarcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uynne.lunarcalendar.ui.LunarCalendarApp
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LunarCalendarTheme {
                LunarCalendarApp()
            }
        }
    }
}
