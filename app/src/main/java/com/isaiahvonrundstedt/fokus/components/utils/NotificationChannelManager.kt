package com.isaiahvonrundstedt.fokus.components.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager

class NotificationChannelManager(private var context: Context?) {

    private val manager by lazy {
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    fun create(id: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, context?.getString(getResourceString(id)),
                NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(notificationSoundUri, attributes)
            manager?.createNotificationChannel(channel)
        }
    }

    @StringRes
    private fun getResourceString(id: String): Int {
        return when (id) {
            CHANNEL_ID_TASK -> R.string.notification_channel_task_reminders
            CHANNEL_ID_EVENT -> R.string.notification_channel_event_reminders
            CHANNEL_ID_CLASS -> R.string.notification_channel_class_reminders
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
        const val CHANNEL_ID_GENERIC = "channel:generic"
    }
}