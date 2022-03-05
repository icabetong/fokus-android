package com.isaiahvonrundstedt.fokus.features.notifications.event

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// This worker's function is to reschedule all pending workers
// that is supposed to trigger at its due minus the interval
// This only triggers when the user has changed the fokus interval
// for tasks in the Settings

@HiltWorker
class EventNotificationScheduler @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: EventRepository
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val items = repository.fetchCore()

        items.forEach { event ->
            if (event.schedule?.isAfterNow() == true) {
                val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                    .setInputData(convertEventToData(event))
                    .build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    event.eventID,
                    ExistingWorkPolicy.REPLACE, request
                )
            }
        }
        return Result.success()
    }
}