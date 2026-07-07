package com.uynne.lunarcalendar.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider
import com.uynne.lunarcalendar.ui.settings.AppearancePrefs
import com.uynne.lunarcalendar.ui.theme.AppearanceMode
import com.uynne.lunarcalendar.ui.theme.DarkColors
import com.uynne.lunarcalendar.ui.theme.LightColors

val KEY_WIDGET_THEME = stringPreferencesKey("widget_theme")
val KEY_WIDGET_THEME_MODE = stringPreferencesKey("widget_theme_mode")
val KEY_WIDGET_STYLE = stringPreferencesKey("widget_style")
val KEY_WIDGET_ACCENT_COLOR = stringPreferencesKey("widget_accent_color")

const val WIDGET_THEME_SYSTEM = "system"
const val WIDGET_THEME_LIGHT = "light"
const val WIDGET_THEME_DARK = "dark"
const val WIDGET_THEME_MATCH_APP = "match_app"

enum class WidgetThemeMode(val storedValue: String, val label: String) {
    SYSTEM(WIDGET_THEME_SYSTEM, "Theo hệ thống"),
    LIGHT(WIDGET_THEME_LIGHT, "Sáng"),
    DARK(WIDGET_THEME_DARK, "Tối"),
    MATCH_APP(WIDGET_THEME_MATCH_APP, "Theo ứng dụng");

    companion object {
        fun fromStoredValue(value: String?): WidgetThemeMode =
            entries.firstOrNull { it.storedValue == value } ?: SYSTEM
    }
}

enum class WidgetStyle(val storedValue: String, val label: String) {
    MINIMAL("minimal", "Tối giản"),
    CALENDAR("calendar", "Lịch"),
    LUNAR("lunar", "Âm lịch"),
    MOON("moon", "Trăng"),
    COMBINED("combined", "Kết hợp");

    companion object {
        fun fromStoredValue(value: String?): WidgetStyle =
            entries.firstOrNull { it.storedValue == value } ?: COMBINED
    }
}

enum class WidgetAccentColor(
    val storedValue: String,
    val label: String,
    val light: Color,
    val dark: Color,
) {
    BLUE("blue", "Xanh", Color(0xFF007AFF), Color(0xFF6CAEFF)),
    PINK("pink", "Hồng", Color(0xFFFF2D55), Color(0xFFFF7A9D)),
    GREEN("green", "Lục", Color(0xFF34C759), Color(0xFF6DE08A)),
    WARM("warm", "Ấm", Color(0xFFFF9500), Color(0xFFFFB84D)),
    MONOCHROME("monochrome", "Đơn sắc", Color(0xFF3A3A3C), Color(0xFFE5E5EA));

    fun glanceColor(): ColorProvider = ColorProvider(light)

    companion object {
        fun fromStoredValue(value: String?): WidgetAccentColor =
            entries.firstOrNull { it.storedValue == value } ?: BLUE
    }
}

object WidgetTheme {
    val system = ColorProviders(light = LightColors, dark = DarkColors)
    private val lightOnly = ColorProviders(light = LightColors, dark = LightColors)
    private val darkOnly = ColorProviders(light = DarkColors, dark = DarkColors)

    fun fromPref(value: String?) =
        fromMode(WidgetThemeMode.fromStoredValue(value), AppearanceMode.SYSTEM)

    fun fromMode(mode: WidgetThemeMode, appAppearanceMode: AppearanceMode) =
        when (mode) {
            WidgetThemeMode.LIGHT -> lightOnly
            WidgetThemeMode.DARK -> darkOnly
            WidgetThemeMode.MATCH_APP -> when (appAppearanceMode) {
                AppearanceMode.LIGHT -> lightOnly
                AppearanceMode.DARK -> darkOnly
                AppearanceMode.SYSTEM -> system
            }
            WidgetThemeMode.SYSTEM -> system
        }
}

object WidgetDefaultsPrefs {
    private const val FILE_NAME = "widget_defaults"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_STYLE = "style"
    private const val KEY_ACCENT = "accent"

    data class Defaults(
        val themeMode: WidgetThemeMode,
        val style: WidgetStyle,
        val accentColor: WidgetAccentColor,
    )

    fun get(context: Context): Defaults {
        val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return Defaults(
            themeMode = WidgetThemeMode.fromStoredValue(prefs.getString(KEY_THEME_MODE, null)),
            style = WidgetStyle.fromStoredValue(prefs.getString(KEY_STYLE, null)),
            accentColor = WidgetAccentColor.fromStoredValue(prefs.getString(KEY_ACCENT, null)),
        )
    }

    fun set(context: Context, defaults: Defaults) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, defaults.themeMode.storedValue)
            .putString(KEY_STYLE, defaults.style.storedValue)
            .putString(KEY_ACCENT, defaults.accentColor.storedValue)
            .apply()
    }
}

fun appAppearanceForWidgets(context: Context): AppearanceMode = AppearancePrefs.get(context)
