package com.isaiahvonrundstedt.fokus.features.core.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

class LocalizationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.let {
                    val manager = it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()

                    val notificationSoundUri: Uri = PreferenceManager(it).run {
                        if (customSoundEnabled) customSoundUri
                        else PreferenceManager.DEFAULT_SOUND_URI
                    }

                    with(NotificationManagerCompat.from(it)) {
                        if (getNotificationChannel(BaseWorker.NOTIFICATION_CHANNEL_ID_TASK) != null) {
                            manager.createNotificationChannel(
                                NotificationChannel(BaseWorker.NOTIFICATION_CHANNEL_ID_TASK,
                                    it.getString(R.string.notification_channel_task_reminders),
                                    NotificationManager.IMPORTANCE_HIGH).apply { setSound(notificationSoundUri, attributes) }
                            )
                        }

                        if (getNotificationChannel(BaseWorker.NOTIFICATION_CHANNEL_ID_EVENT) != null) {
                            manager.createNotificationChannel(
                                NotificationChannel(BaseWorker.NOTIFICATION_CHANNEL_ID_EVENT,
                                    it.getString(R.string.notification_channel_event_reminders),
                                    NotificationManager.IMPORTANCE_HIGH).apply { setSound(notificationSoundUri, attributes) }
                            )
                        }

                        if (getNotificationChannel(BaseWorker.NOTIFICATION_CHANNEL_ID_GENERIC) != null) {
                            manager.createNotificationChannel(
                                NotificationChannel(BaseWorker.NOTIFICATION_CHANNEL_ID_GENERIC,
                                    it.getString(R.string.notification_channel_generic),
                                    NotificationManager.IMPORTANCE_HIGH).apply { setSound(notificationSoundUri, attributes) }
                            )
                        }
                    }
                }
            }
        }
    }
}