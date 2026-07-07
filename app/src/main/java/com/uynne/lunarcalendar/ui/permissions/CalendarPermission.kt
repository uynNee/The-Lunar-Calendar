package com.uynne.lunarcalendar.ui.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

private val CALENDAR_PERMISSIONS = arrayOf(
    Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR,
)

@Stable
class CalendarPermissionState(
    val granted: Boolean,
    val deniedOnce: Boolean,
    val request: () -> Unit,
    val openSettings: () -> Unit,
)

@Composable
fun rememberCalendarPermissionState(): CalendarPermissionState {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            CALENDAR_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            },
        )
    }
    var deniedOnce by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        granted = results.values.all { it }
        if (!granted) deniedOnce = true
    }

    // Returning from Settings (or any resume) re-checks the actual grant state.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = CALENDAR_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    return CalendarPermissionState(
        granted = granted,
        deniedOnce = deniedOnce,
        request = { launcher.launch(CALENDAR_PERMISSIONS) },
        openSettings = {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ),
            )
        },
    )
}
