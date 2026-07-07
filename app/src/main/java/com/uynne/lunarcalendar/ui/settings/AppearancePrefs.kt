package com.uynne.lunarcalendar.ui.settings

import android.content.Context
import com.uynne.lunarcalendar.ui.theme.AppearanceMode

object AppearancePrefs {
    private const val FILE_NAME = "appearance"
    private const val KEY_MODE = "mode"

    fun get(context: Context): AppearanceMode =
        AppearanceMode.fromStoredValue(
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(KEY_MODE, null),
        )

    fun set(context: Context, mode: AppearanceMode) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.storedValue)
            .apply()
    }
}
