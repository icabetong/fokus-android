package com.isaiahvonrundstedt.fokus.components.extensions.android

import android.content.Context
import android.content.Intent
import android.os.Build

fun Context.startForegroundServiceCompat(service: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        startForegroundService(service)
    else startService(service)
}