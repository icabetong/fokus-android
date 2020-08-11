package com.isaiahvonrundstedt.fokus.components.extensions.android

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun Context.startForegroundServiceCompat(service: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        startForegroundService(service)
    else startService(service)
}