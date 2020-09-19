package com.isaiahvonrundstedt.fokus.features.notifications

import android.content.Context
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import java.time.ZonedDateTime

// This worker fetches the fokus passed by various
// worker classes. It's primary purpose is to only trigger
// and to show the fokus. Also to insert the fokus
// object to the database.
class NotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var notifications = AppDatabase.getInstance(applicationContext).logs()

    override suspend fun doWork(): Result {
        val notification = convertDataToLog(inputData)
        notification.dateTimeTriggered = ZonedDateTime.now()

        notifications.insert(notification)
        if (notification.isImportant)
            sendNotification(notification, notification.data)
        else sendNotification(notification)

        return Result.success()
    }
}