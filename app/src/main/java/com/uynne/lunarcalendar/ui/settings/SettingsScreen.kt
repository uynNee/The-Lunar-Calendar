package com.uynne.lunarcalendar.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.theme.AppearanceMode
import com.uynne.lunarcalendar.widget.WidgetAccentColor
import com.uynne.lunarcalendar.widget.WidgetDefaultsPrefs
import com.uynne.lunarcalendar.widget.WidgetStyle
import com.uynne.lunarcalendar.widget.WidgetThemeMode

private enum class PickerTarget {
    APP_APPEARANCE,
    WIDGET_THEME,
    WIDGET_STYLE,
    WIDGET_ACCENT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appearanceMode: AppearanceMode,
    onAppearanceModeChange: (AppearanceMode) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var widgetDefaults by remember {
        mutableStateOf(WidgetDefaultsPrefs.get(context))
    }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    fun updateWidgetDefaults(defaults: WidgetDefaultsPrefs.Defaults) {
        widgetDefaults = defaults
        WidgetDefaultsPrefs.set(context, defaults)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Lịch Âm",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )

            GroupedSection(title = "Giao diện") {
                GroupedRow(
                    label = "Ứng dụng",
                    value = appearanceMode.label,
                    onClick = { pickerTarget = PickerTarget.APP_APPEARANCE },
                )
                RowDivider()
                GroupedRow(
                    label = "Widget",
                    value = widgetDefaults.themeMode.label,
                    onClick = { pickerTarget = PickerTarget.WIDGET_THEME },
                )
            }

            GroupedSection(title = "Widget") {
                GroupedRow(
                    label = "Kiểu",
                    value = widgetDefaults.style.label,
                    onClick = { pickerTarget = PickerTarget.WIDGET_STYLE },
                )
                RowDivider()
                GroupedRow(
                    label = "Màu nhấn",
                    value = widgetDefaults.accentColor.label,
                    leading = { AccentDot(widgetDefaults.accentColor) },
                    onClick = { pickerTarget = PickerTarget.WIDGET_ACCENT },
                )
            }

            GroupedSection(title = "Lịch") {
                GroupedRow(
                    label = "Tuần bắt đầu",
                    value = "Thứ Hai",
                )
                RowDivider()
                GroupedRow(
                    label = "Google Calendar",
                    value = "Bật khi cấp quyền",
                )
            }
        }
    }

    when (pickerTarget) {
        PickerTarget.APP_APPEARANCE -> OptionDialog(
            title = "Giao diện ứng dụng",
            options = AppearanceMode.entries,
            selected = appearanceMode,
            label = { it.label },
            onSelect = {
                onAppearanceModeChange(it)
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null },
        )
        PickerTarget.WIDGET_THEME -> OptionDialog(
            title = "Giao diện widget",
            options = WidgetThemeMode.entries,
            selected = widgetDefaults.themeMode,
            label = { it.label },
            onSelect = {
                updateWidgetDefaults(widgetDefaults.copy(themeMode = it))
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null },
        )
        PickerTarget.WIDGET_STYLE -> OptionDialog(
            title = "Kiểu widget",
            options = WidgetStyle.entries,
            selected = widgetDefaults.style,
            label = { it.label },
            onSelect = {
                updateWidgetDefaults(widgetDefaults.copy(style = it))
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null },
        )
        PickerTarget.WIDGET_ACCENT -> OptionDialog(
            title = "Màu nhấn",
            options = WidgetAccentColor.entries,
            selected = widgetDefaults.accentColor,
            label = { it.label },
            leading = { AccentDot(it) },
            onSelect = {
                updateWidgetDefaults(widgetDefaults.copy(accentColor = it))
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null },
        )
        null -> Unit
    }
}

@Composable
private fun AccentDot(accent: WidgetAccentColor) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(accent.light),
    )
}

@Composable
private fun <T> OptionDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    leading: @Composable ((T) -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    GroupedRow(
                        label = label(option),
                        leading = leading?.let { { it(option) } },
                        onClick = { onSelect(option) },
                        trailing = {
                            RadioButton(
                                selected = selected == option,
                                onClick = { onSelect(option) },
                            )
                        },
                    )
                    if (index != options.lastIndex) RowDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Xong")
            }
        },
    )
}
