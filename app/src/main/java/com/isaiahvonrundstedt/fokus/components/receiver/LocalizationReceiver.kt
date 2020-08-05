package com.isaiahvonrundstedt.fokus.components.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.isaiahvonrundstedt.fokus.components.utils.AppNotificationManager

class LocalizationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
            with(AppNotificationManager(context)) {
                create(AppNotificationManager.CHANNEL_ID_TASK)
                create(AppNotificationManager.CHANNEL_ID_EVENT)
                create(AppNotificationManager.CHANNEL_ID_GENERIC)
            }
        }
    }
}