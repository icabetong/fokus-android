package com.isaiahvonrundstedt.fokus.features.notifications.event

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

// This worker's function is to schedule the fokus worker
// for the event schedule minus the interval.

@HiltWorker
class EventNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val event = convertDataToEvent(inputData)
        val notification = Log().apply {
            title = event.name
            content = event.formatSchedule(applicationContext)
            type = Log.TYPE_EVENT
            data = event.eventID
            isImportant = event.isImportant
        }

        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        request.setInputData(convertLogToData(notification))

        if (notification.isImportant) {
            workManager.enqueueUniqueWork(
                event.eventID, ExistingWorkPolicy.REPLACE,
                request.build()
            )
            return Result.success()
        }

        var executionTime = event.schedule
        when (preferenceManager.eventReminderInterval) {
            PreferenceManager.EVENT_REMINDER_INTERVAL_15_MINUTES ->
                executionTime = event.schedule?.minusMinutes(15)
            PreferenceManager.EVENT_REMINDER_INTERVAL_30_MINUTES ->
                executionTime = event.schedule?.minusMinutes(30)
            PreferenceManager.EVENT_REMINDER_INTERVAL_60_MINUTES ->
                executionTime = event.schedule?.minusMinutes(60)
        }

        if (executionTime?.isAfterNow() == true)
            request.setInitialDelay(
                Duration.between(ZonedDateTime.now(), executionTime).toMinutes(),
                TimeUnit.MINUTES
            )

        workManager.enqueueUniqueWork(
            event.eventID, ExistingWorkPolicy.REPLACE,
            request.build()
        )

        return Result.success()
    }
}