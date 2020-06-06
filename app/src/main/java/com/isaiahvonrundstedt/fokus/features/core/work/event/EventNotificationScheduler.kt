package com.isaiahvonrundstedt.fokus.features.core.work.event

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime

// This worker's function is to reschedule all pending workers
// that is supposed to trigger at its due minus the interval
// This only triggers when the user has changed the fokus interval
// for tasks in the Settings
class EventNotificationScheduler(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var events = AppDatabase.getInstance(applicationContext)?.events()

    override suspend fun doWork(): Result {
        val items = events?.fetch()
        val currentDateTime = DateTime.now()

        items?.forEach { event ->
            if (event.schedule!!.isAfter(currentDateTime)) {
                val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                    .setInputData(convertEventToData(event))
                    .build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                    request)
            }
        }
        return Result.success()
    }
}