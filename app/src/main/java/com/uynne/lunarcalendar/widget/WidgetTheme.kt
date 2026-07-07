package com.uynne.lunarcalendar.widget

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.material3.ColorProviders
import com.uynne.lunarcalendar.ui.theme.DarkColors
import com.uynne.lunarcalendar.ui.theme.LightColors

val KEY_WIDGET_THEME = stringPreferencesKey("widget_theme")

const val WIDGET_THEME_SYSTEM = "system"
const val WIDGET_THEME_LIGHT = "light"
const val WIDGET_THEME_DARK = "dark"

object WidgetTheme {
    val system = ColorProviders(light = LightColors, dark = DarkColors)
    private val lightOnly = ColorProviders(light = LightColors, dark = LightColors)
    private val darkOnly = ColorProviders(light = DarkColors, dark = DarkColors)

    fun fromPref(value: String?) = when (value) {
        WIDGET_THEME_LIGHT -> lightOnly
        WIDGET_THEME_DARK -> darkOnly
        else -> system
    }
}
