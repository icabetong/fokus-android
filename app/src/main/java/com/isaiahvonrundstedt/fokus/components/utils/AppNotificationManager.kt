package com.isaiahvonrundstedt.fokus.components.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager

class AppNotificationManager(private var context: Context?) {

    private var manager: NotificationManager?
            = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    @RequiresApi(26)
    fun channelExists(id: String): Boolean {
        return manager?.getNotificationChannel(id) == null
    }

    @RequiresApi(26)
    fun recreateChannel(id: String, importance: Int = NotificationManager.IMPORTANCE_HIGH) {
        manager?.createNotificationChannel(
            NotificationChannel(id, context?.getString(getResourceString(id)),
                importance).apply {
                setSound(notificationSoundUri, attributes)
            })
    }

    @RequiresApi(26)
    fun createChannel(id: String, importance: Int = NotificationManager.IMPORTANCE_HIGH) {
        if (!channelExists(id))
            recreateChannel(id, importance)
    }

    @StringRes
    private fun getResourceString(id: String): Int {
        return when (id) {
            CHANNEL_ID_TASK -> R.string.notification_channel_task_reminders
            CHANNEL_ID_EVENT -> R.string.notification_channel_event_reminders
            CHANNEL_ID_CLASS -> R.string.notification_channel_class_reminders
            CHANNEL_ID_BACKUP -> R.string.notification_channel_backups
            CHANNEL_ID_GENERIC -> R.string.notification_channel_generic
            else -> 0
        }
    }

    private val attributes: AudioAttributes
        get() = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

    private val notificationSoundUri: Uri
        get() {
            return PreferenceManager(context).let {
                if (it.customSoundEnabled) it.customSoundUri
                else PreferenceManager.DEFAULT_SOUND_URI
            }
        }

    companion object {
        const val CHANNEL_ID_TASK = "channel:task"
        const val CHANNEL_ID_EVENT = "channel:event"
        const val CHANNEL_ID_CLASS = "channel:class"
        const val CHANNEL_ID_BACKUP = "channel:backup"
        const val CHANNEL_ID_GENERIC = "channel:generic"
    }
}