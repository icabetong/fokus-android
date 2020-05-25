package com.isaiahvonrundstedt.fokus.features.core.work

import android.app.Application
import android.content.Context
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.NotificationRepository
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime

class NotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private val dataStore by lazy {
        NotificationRepository.getInstance(context.applicationContext as Application)
    }

    override suspend fun doWork(): Result  {
        val notification = convertDataToNotification(inputData)
        notification.dateTimeTriggered = DateTime.now()

        dataStore.insert(notification)
        sendNotification(notification)

        return Result.success()
    }
}