package com.uynne.lunarcalendar.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import com.uynne.lunarcalendar.widget.KEY_WIDGET_ACCENT_COLOR
import com.uynne.lunarcalendar.widget.KEY_WIDGET_STYLE
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME_MODE
import com.uynne.lunarcalendar.widget.WidgetAccentColor
import com.uynne.lunarcalendar.widget.WidgetDefaultsPrefs
import com.uynne.lunarcalendar.widget.WidgetRefresh
import com.uynne.lunarcalendar.widget.WidgetStyle
import com.uynne.lunarcalendar.widget.WidgetThemeMode
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val defaults = WidgetDefaultsPrefs.get(this)
        setContent {
            LunarCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfigContent(
                        defaults = defaults,
                        onConfirm = ::saveAndFinish,
                        onCancel = { finish() },
                    )
                }
            }
        }
    }

    private fun saveAndFinish(
        themeMode: WidgetThemeMode,
        style: WidgetStyle,
        accentColor: WidgetAccentColor,
    ) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity)
                .getGlanceIdBy(appWidgetId)
            updateAppWidgetState(
                this@WidgetConfigActivity,
                PreferencesGlanceStateDefinition,
                glanceId,
            ) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_WIDGET_THEME_MODE] = themeMode.storedValue
                    this[KEY_WIDGET_THEME] = themeMode.storedValue
                    this[KEY_WIDGET_STYLE] = style.storedValue
                    this[KEY_WIDGET_ACCENT_COLOR] = accentColor.storedValue
                }
            }
            WidgetRefresh.updateAllWidgets(this@WidgetConfigActivity)
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
            )
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigContent(
    defaults: WidgetDefaultsPrefs.Defaults,
    onConfirm: (WidgetThemeMode, WidgetStyle, WidgetAccentColor) -> Unit,
    onCancel: () -> Unit,
) {
    var selectedTheme by remember { mutableStateOf(defaults.themeMode) }
    var selectedStyle by remember { mutableStateOf(defaults.style) }
    var selectedAccent by remember { mutableStateOf(defaults.accentColor) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt widget", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Hủy") }
                },
                actions = {
                    TextButton(onClick = { onConfirm(selectedTheme, selectedStyle, selectedAccent) }) {
                        Text("Xong")
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WidgetPreviewCard(
                style = selectedStyle,
                themeMode = selectedTheme,
                accentColor = selectedAccent,
            )

            GroupedSection(title = "Kiểu") {
                WidgetStyle.entries.forEachIndexed { index, style ->
                    GroupedRow(
                        label = style.label,
                        supportingText = style.previewText,
                        onClick = { selectedStyle = style },
                        trailing = {
                            RadioButton(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                            )
                        },
                    )
                    if (index != WidgetStyle.entries.lastIndex) RowDivider()
                }
            }

            GroupedSection(title = "Giao diện") {
                WidgetThemeMode.entries.forEachIndexed { index, theme ->
                    GroupedRow(
                        label = theme.label,
                        onClick = { selectedTheme = theme },
                        trailing = {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = { selectedTheme = theme },
                            )
                        },
                    )
                    if (index != WidgetThemeMode.entries.lastIndex) RowDivider()
                }
            }

            GroupedSection(title = "Màu") {
                WidgetAccentColor.entries.forEachIndexed { index, accent ->
                    GroupedRow(
                        label = accent.label,
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(accent.light),
                            )
                        },
                        onClick = { selectedAccent = accent },
                        trailing = {
                            RadioButton(
                                selected = selectedAccent == accent,
                                onClick = { selectedAccent = accent },
                            )
                        },
                    )
                    if (index != WidgetAccentColor.entries.lastIndex) RowDivider()
                }
            }
        }
    }
}

@Composable
private fun WidgetPreviewCard(
    style: WidgetStyle,
    themeMode: WidgetThemeMode,
    accentColor: WidgetAccentColor,
) {
    val dark = themeMode == WidgetThemeMode.DARK
    val cardColor = if (dark) androidx.compose.ui.graphics.Color(0xFF1C1C1E) else androidx.compose.ui.graphics.Color.White
    val textColor = if (dark) androidx.compose.ui.graphics.Color(0xFFF5F5F7) else androidx.compose.ui.graphics.Color(0xFF111113)
    val secondary = if (dark) androidx.compose.ui.graphics.Color(0xFFAEAEB2) else androidx.compose.ui.graphics.Color(0xFF6E6E73)
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = style.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = secondary,
                )
                Text(
                    text = "7",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor.light,
                )
                Text(
                    text = previewMainText(style),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                )
                Text(
                    text = "Ngày Nhâm Ngọ · Năm Bính Ngọ",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(accentColor.light.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🌗", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

private val WidgetStyle.previewText: String
    get() = when (this) {
        WidgetStyle.MINIMAL -> "Ngày và âm lịch thật gọn"
        WidgetStyle.CALENDAR -> "Lưới tháng rõ hơn"
        WidgetStyle.LUNAR -> "Âm lịch nổi bật"
        WidgetStyle.MOON -> "Pha trăng và ngày rằm"
        WidgetStyle.COMBINED -> "Thông tin cân bằng"
    }

private fun previewMainText(style: WidgetStyle): String =
    when (style) {
        WidgetStyle.MINIMAL -> "Thứ Ba"
        WidgetStyle.CALENDAR -> "Tháng 7 2026"
        WidgetStyle.LUNAR -> "Ngày 23 tháng 5 ÂL"
        WidgetStyle.MOON -> "Trăng hạ huyền"
        WidgetStyle.COMBINED -> "Ngày 23 tháng 5 ÂL"
    }
