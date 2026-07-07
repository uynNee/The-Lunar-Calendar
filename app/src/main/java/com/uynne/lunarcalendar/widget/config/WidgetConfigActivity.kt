package com.uynne.lunarcalendar.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.uynne.lunarcalendar.ui.components.GroupedRow
import com.uynne.lunarcalendar.ui.components.GroupedSection
import com.uynne.lunarcalendar.ui.components.RowDivider
import com.uynne.lunarcalendar.ui.theme.LunarCalendarTheme
import com.uynne.lunarcalendar.widget.KEY_WIDGET_THEME
import com.uynne.lunarcalendar.widget.WIDGET_THEME_DARK
import com.uynne.lunarcalendar.widget.WIDGET_THEME_LIGHT
import com.uynne.lunarcalendar.widget.WIDGET_THEME_SYSTEM
import com.uynne.lunarcalendar.widget.WidgetRefresh
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
        setContent {
            LunarCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfigContent(
                        onConfirm = ::saveAndFinish,
                        onCancel = { finish() },
                    )
                }
            }
        }
    }

    private fun saveAndFinish(theme: String) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity)
                .getGlanceIdBy(appWidgetId)
            updateAppWidgetState(
                this@WidgetConfigActivity,
                PreferencesGlanceStateDefinition,
                glanceId,
            ) { prefs ->
                prefs.toMutablePreferences().apply { this[KEY_WIDGET_THEME] = theme }
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
private fun ConfigContent(onConfirm: (String) -> Unit, onCancel: () -> Unit) {
    val options = listOf(
        WIDGET_THEME_SYSTEM to "Theo hệ thống",
        WIDGET_THEME_LIGHT to "Sáng",
        WIDGET_THEME_DARK to "Tối",
    )
    var selected by remember { mutableStateOf(WIDGET_THEME_SYSTEM) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt widget", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Hủy") }
                },
                actions = {
                    TextButton(onClick = { onConfirm(selected) }) { Text("Xong") }
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Giao diện",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            GroupedSection {
                options.forEachIndexed { index, (value, label) ->
                    GroupedRow(
                        label = label,
                        onClick = { selected = value },
                        trailing = {
                            RadioButton(
                                selected = selected == value,
                                onClick = { selected = value },
                            )
                        },
                    )
                    if (index != options.lastIndex) RowDivider()
                }
            }
        }
    }
}
