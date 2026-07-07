package com.uynne.lunarcalendar.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
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
                    ConfigContent(onConfirm = ::saveAndFinish)
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

@androidx.compose.runtime.Composable
private fun ConfigContent(onConfirm: (String) -> Unit) {
    val options = listOf(
        WIDGET_THEME_SYSTEM to "Theo hệ thống",
        WIDGET_THEME_LIGHT to "Sáng",
        WIDGET_THEME_DARK to "Tối",
    )
    var selected by remember { mutableStateOf(WIDGET_THEME_SYSTEM) }
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Cài đặt widget",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Giao diện",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        options.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = selected == value, onClick = { selected = value })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == value, onClick = { selected = value })
                Text(text = label, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Button(
            onClick = { onConfirm(selected) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text("Xong")
        }
    }
}
