package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationRepository(app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var notifications = database?.notification()

    fun fetch(): LiveData<List<Notification>>? = notifications?.fetch()

    fun clear() = GlobalScope.launch { notifications?.clear() }

    suspend fun insert(notification: Notification) {
        notifications?.insert(notification)
    }

    suspend fun remove(notification: Notification) {
        notifications?.remove(notification)
    }

    suspend fun update(notification: Notification) {
        notifications?.update(notification)
    }

}