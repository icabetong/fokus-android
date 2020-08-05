package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import com.isaiahvonrundstedt.fokus.components.utils.AppNotificationManager

abstract class BaseService: Service() {

    protected fun sendNotification(id: Int, notification: Notification) {
        val channel = AppNotificationManager(this)
        channel.create(AppNotificationManager.CHANNEL_ID_BACKUP)
        manager?.notify(id, notification)
    }

    protected val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

}