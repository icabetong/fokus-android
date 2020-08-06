package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.service.BackupRestoreService
import com.isaiahvonrundstedt.fokus.components.utils.AppNotificationManager

abstract class BaseService: Service() {

    protected fun startForegroundCompat(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(AppNotificationManager(this)) {
                create(AppNotificationManager.CHANNEL_ID_BACKUP)
            }

            startForeground(id, notification)
        }
        else manager?.notify(id, notification)
    }

    protected fun stopForegroundCompat(id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)
        else manager?.cancel(id)
    }

    protected fun createNotification(@StringRes id: Int, @StringRes content: Int = 0,
                                   ongoing: Boolean = false): Notification {
        return NotificationCompat.Builder(this,
            AppNotificationManager.CHANNEL_ID_BACKUP).apply {
            setSmallIcon(R.drawable.ic_outline_done_all_24)
            setContentTitle(getString(id))
            if (content != 0) setContentText(getString(content))
            setOngoing(ongoing)
            setCategory(Notification.CATEGORY_SERVICE)
            setChannelId(AppNotificationManager.CHANNEL_ID_BACKUP)
            if (ongoing) setProgress(0, 0, true)
            color = ContextCompat.getColor(this@BaseService, R.color.color_primary)
        }.build()
    }

    protected fun terminateService(status: String? = null) {
        if (status != null)
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ACTION_SERVICE_BROADCAST).apply {
                    putExtra(EXTRA_BROADCAST_STATUS, status)
                })
        stopSelf()
    }

    protected val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    companion object {
        const val ACTION_SERVICE_BROADCAST = "action:service:status"
        const val EXTRA_BROADCAST_STATUS = "extra:broadcast:status"
    }

}