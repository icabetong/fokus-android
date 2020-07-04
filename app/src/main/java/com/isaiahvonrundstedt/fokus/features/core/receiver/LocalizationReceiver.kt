package com.isaiahvonrundstedt.fokus.features.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.isaiahvonrundstedt.fokus.components.utils.NotificationChannelManager

class LocalizationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
            with(NotificationChannelManager(context)) {
                create(NotificationChannelManager.CHANNEL_ID_TASK)
                create(NotificationChannelManager.CHANNEL_ID_EVENT)
                create(NotificationChannelManager.CHANNEL_ID_GENERIC)
            }
        }
    }
}