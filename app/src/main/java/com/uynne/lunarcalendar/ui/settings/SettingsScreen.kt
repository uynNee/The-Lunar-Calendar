package com.uynne.lunarcalendar.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uynne.lunarcalendar.data.calendar.CalendarGraph
import com.uynne.lunarcalendar.ui.calendars.CalendarPickerSheet
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.month.MonthViewModel
import com.uynne.lunarcalendar.ui.permissions.rememberCalendarPermissionState
import com.uynne.lunarcalendar.ui.theme.AppearanceMode
import com.uynne.lunarcalendar.ui.theme.Dimens
import com.uynne.lunarcalendar.widget.WidgetAccentColor
import com.uynne.lunarcalendar.widget.WidgetDefaultsPrefs
import com.uynne.lunarcalendar.widget.WidgetRefresh
import com.uynne.lunarcalendar.widget.WidgetStyle
import com.uynne.lunarcalendar.widget.WidgetThemeMode
import java.time.DayOfWeek
import kotlinx.coroutines.launch

private enum class PickerTarget {
    APP_APPEARANCE,
    WIDGET_THEME,
    WIDGET_STYLE,
    WIDGET_ACCENT,
    WEEK_START,
}

private val DayOfWeek.label: String
    get() = if (this == DayOfWeek.SUNDAY) "Chủ Nhật" else "Thứ Hai"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appearanceMode: AppearanceMode,
    onAppearanceModeChange: (AppearanceMode) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calendarPrefs = remember { CalendarGraph.prefs(context) }
    var widgetDefaults by remember {
        mutableStateOf(WidgetDefaultsPrefs.get(context))
    }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    val weekStart by calendarPrefs.weekStart.collectAsStateWithLifecycle()
    val permission = rememberCalendarPermissionState()
    val monthViewModel: MonthViewModel = viewModel(factory = MonthViewModel.Factory)
    val calendars by monthViewModel.calendars.collectAsStateWithLifecycle()
    val hiddenIds by monthViewModel.hiddenIds.collectAsStateWithLifecycle()

    fun updateWidgetDefaults(defaults: WidgetDefaultsPrefs.Defaults) {
        widgetDefaults = defaults
        WidgetDefaultsPrefs.set(context, defaults)
        scope.launch { WidgetRefresh.pushDefaultsToAllWidgets(context, defaults) }
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
                .padding(horizontal = Dimens.spaceMD, vertical = Dimens.spaceXS),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSM),
        ) {
            Text(
                text = "Lịch Âm",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Dimens.spaceXXS, top = Dimens.spaceXXS),
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
                    trailing = { AccentDot(widgetDefaults.accentColor) },
                    onClick = { pickerTarget = PickerTarget.WIDGET_ACCENT },
                )
            }

            GroupedSection(title = "Lịch") {
                GroupedRow(
                    label = "Ngày đầu tuần",
                    value = weekStart.label,
                    onClick = { pickerTarget = PickerTarget.WEEK_START },
                )
                RowDivider()
                GroupedRow(
                    label = "Lịch hiển thị",
                    value = if (permission.granted) null else "Cần cấp quyền",
                    onClick = {
                        if (permission.granted) {
                            monthViewModel.refreshCalendars()
                            showCalendarPicker = true
                        } else if (permission.deniedOnce) {
                            permission.openSettings()
                        } else {
                            permission.request()
                        }
                    },
                )
                RowDivider()
                GroupedRow(
                    label = "Google Calendar",
                    value = if (permission.granted) "Đã cấp quyền" else "Chưa cấp quyền",
                    onClick = if (permission.granted) {
                        null
                    } else if (permission.deniedOnce) {
                        permission.openSettings
                    } else {
                        permission.request
                    },
                )
            }
        }
    }

    if (showCalendarPicker) {
        CalendarPickerSheet(
            calendars = calendars,
            hiddenIds = hiddenIds,
            onToggle = monthViewModel::toggleCalendar,
            onDismiss = { showCalendarPicker = false },
        )
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
        PickerTarget.WEEK_START -> OptionDialog(
            title = "Ngày đầu tuần",
            options = listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY),
            selected = weekStart,
            label = { it.label },
            onSelect = {
                calendarPrefs.setWeekStart(it)
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
            .size(Dimens.iconSM)
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
