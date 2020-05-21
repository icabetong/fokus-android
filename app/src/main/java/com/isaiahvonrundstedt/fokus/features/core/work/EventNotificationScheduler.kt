package com.isaiahvonrundstedt.fokus.features.core.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime

class EventNotificationScheduler(private var context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var events = AppDatabase.getInstance(context)?.events()

    override suspend fun doWork(): Result {
        val items = events?.fetchList()
        val currentDateTime = DateTime.now()

        items?.forEach { event ->
            if (event.schedule!!.isAfter(currentDateTime)) {
                val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                    .setInputData(convertEventToData(event))
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(event.id, ExistingWorkPolicy.REPLACE,
                    request)
            }
        }
        return Result.success()
    }
}