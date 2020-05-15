package com.isaiahvonrundstedt.fokus.features.core.work

import android.app.Application
import android.content.Context
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.NotificationRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

class NotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private val dataStore by lazy {
        NotificationRepository(context.applicationContext as Application)
    }

    override suspend fun doWork(): Result  {
        val notification = convertDataToNotification(inputData)

        dataStore.insert(notification)
        sendNotification(createNotification(notification))

        return Result.success()
    }
}