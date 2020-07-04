package com.isaiahvonrundstedt.fokus.features.core.work.event

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.features.core.work.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.history.History
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

// This worker's function is to schedule the fokus worker
// for the event schedule minus the interval.
class EventNotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val event = convertDataToEvent(inputData)
        val notification = History().apply {
            title = event.name
            content = event.formatSchedule(applicationContext)
            type = History.TYPE_EVENT
            data = event.eventID
            isPersistent = event.isImportant
        }

        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        request.setInputData(convertHistoryToData(notification))

        if (notification.isPersistent) {
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request.build())
            return Result.success()
        }

        var executionTime = event.schedule!!
        when (preferenceManager.eventReminderInterval) {
            PreferenceManager.EVENT_REMINDER_INTERVAL_15_MINUTES ->
                executionTime = event.schedule!!.minusMinutes(15)
            PreferenceManager.EVENT_REMINDER_INTERVAL_30_MINUTES ->
                executionTime = event.schedule!!.minusMinutes(30)
            PreferenceManager.EVENT_REMINDER_INTERVAL_60_MINUTES ->
                executionTime = event.schedule!!.minusMinutes(60)
        }

        if (executionTime.isAfterNow)
            request.setInitialDelay(Duration(DateTime.now(), executionTime).standardMinutes,
                TimeUnit.MINUTES)

        workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
            request.build())

        return Result.success()
    }
}