package com.isaiahvonrundstedt.fokus.components.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.isaiahvonrundstedt.fokus.components.utils.NotificationChannelManager

class LocalizationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with(NotificationChannelManager(context!!)) {
                    register(NotificationChannelManager.CHANNEL_ID_GENERIC,
                        NotificationManager.IMPORTANCE_DEFAULT)
                    register(NotificationChannelManager.CHANNEL_ID_TASK,
                        groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS)
                    register(NotificationChannelManager.CHANNEL_ID_EVENT,
                        groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS)
                    register(NotificationChannelManager.CHANNEL_ID_CLASS,
                        groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS)
                }
            }
        }
    }
}