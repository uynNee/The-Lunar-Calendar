package com.uynne.lunarcalendar.ui.theme

enum class AppearanceMode(val storedValue: String, val label: String) {
    SYSTEM("system", "Theo hệ thống"),
    LIGHT("light", "Sáng"),
    DARK("dark", "Tối");

    companion object {
        fun fromStoredValue(value: String?): AppearanceMode =
            entries.firstOrNull { it.storedValue == value } ?: SYSTEM
    }
}
