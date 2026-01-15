package it.unina.dietiestates.core.presentation.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private const val PREFS_NAME = "notification_prefs"
private const val KEY_PERMISSION_REQUESTED = "permission_requested"

@Composable
fun rememberNotificationPermissionState(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    autoRequest: Boolean = false
): NotificationPermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? Activity
    
    var hasPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }
    
    var permissionWasRequested by remember {
        mutableStateOf(wasPermissionRequested(context))
    }
    
    var canRequestPermission by remember {
        mutableStateOf(canStillRequestPermission(context, activity))
    }

    // Re-check permission when app resumes (e.g., returning from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = checkNotificationPermission(context)
                canRequestPermission = canStillRequestPermission(context, activity)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionWasRequested = true
        setPermissionRequested(context)
        canRequestPermission = canStillRequestPermission(context, activity)
        if (granted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }
    
    val requestPermissionFn: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasPermission = true
            onPermissionGranted()
        }
    }
    
    // Auto-request permission on first launch if enabled
    LaunchedEffect(autoRequest) {
        if (autoRequest && !hasPermission && !permissionWasRequested) {
            requestPermissionFn()
        }
    }

    return remember(hasPermission, permissionWasRequested, canRequestPermission) {
        NotificationPermissionState(
            hasPermission = hasPermission,
            permissionWasRequested = permissionWasRequested,
            canRequestPermission = canRequestPermission,
            requestPermission = requestPermissionFn
        )
    }
}

data class NotificationPermissionState(
    val hasPermission: Boolean,
    val permissionWasRequested: Boolean,
    val canRequestPermission: Boolean,
    val requestPermission: () -> Unit
)

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun wasPermissionRequested(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_PERMISSION_REQUESTED, false)
}

private fun setPermissionRequested(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
}

private fun canStillRequestPermission(context: Context, activity: Activity?): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return false // No need to request on older Android
    }
    
    // If already has permission, no need to request
    if (checkNotificationPermission(context)) {
        return false
    }
    
    // If never requested, we can request
    if (!wasPermissionRequested(context)) {
        return true
    }
    
    // Check if system will show the permission dialog
    return activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
    } ?: false
}

