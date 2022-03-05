package com.isaiahvonrundstedt.fokus.components.utils

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import com.isaiahvonrundstedt.fokus.R

class NotificationChannelManager(private var context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    @RequiresApi(26)
    fun register(
        id: String,
        importance: Int = NotificationManager.IMPORTANCE_HIGH,
        groupID: String? = null
    ) {

        if (groupID != null && manager.getNotificationChannelGroup(groupID) == null) {
            manager.createNotificationChannelGroup(createGroup(groupID))
        }

        manager.createNotificationChannel(createChannel(id, importance, groupID))
    }

    @RequiresApi(26)
    private fun createChannel(
        id: String,
        importance: Int = NotificationManager.IMPORTANCE_HIGH,
        groupID: String?
    ): NotificationChannel {
        return NotificationChannel(id, context.getString(getChannelNameRes(id)), importance).apply {
            setSound(notificationSoundUri, attributes)
            group = groupID
        }
    }

    @RequiresApi(26)
    private fun createGroup(id: String): NotificationChannelGroup {
        return NotificationChannelGroup(id, context.getString(getGroupNameRes(id)))
    }

    @StringRes
    private fun getChannelNameRes(id: String): Int {
        return when (id) {
            CHANNEL_ID_TASK -> R.string.notification_channel_task_reminders
            CHANNEL_ID_EVENT -> R.string.notification_channel_event_reminders
            CHANNEL_ID_CLASS -> R.string.notification_channel_class_reminders
            CHANNEL_ID_GENERIC -> R.string.notification_channel_general
            else -> 0
        }
    }

    @StringRes
    private fun getGroupNameRes(id: String): Int {
        return when (id) {
            CHANNEL_GROUP_ID_REMINDERS -> R.string.notification_channel_group_reminders
            else -> 0
        }
    }

    private val attributes: AudioAttributes
        get() = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

    private val notificationSoundUri: Uri
        get() = Uri.parse(PreferenceManager.DEFAULT_SOUND)

    companion object {
        const val CHANNEL_ID_TASK = "channel:task"
        const val CHANNEL_ID_EVENT = "channel:event"
        const val CHANNEL_ID_CLASS = "channel:class"
        const val CHANNEL_ID_GENERIC = "channel:generic"

        const val CHANNEL_GROUP_ID_REMINDERS = "group:reminders"
    }
}