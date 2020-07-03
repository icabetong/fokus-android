package com.isaiahvonrundstedt.fokus.features.core.work.event

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.features.core.work.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.history.History
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

// This worker's function is to schedule the fokus worker
// for the event schedule minus the interval.
class EventNotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val currentTime = DateTime.now()

        val event = convertDataToEvent(inputData)
        val notification = History().apply {
            title = event.name
            content = event.formatSchedule(applicationContext)
            type = History.TYPE_EVENT
            data = event.eventID
            isPersistent = event.isImportant
        }

        val notificationRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        notificationRequest.setInputData(convertHistoryToData(notification))

        if (notification.isPersistent) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(event.eventID,
                ExistingWorkPolicy.REPLACE, notificationRequest.build())
            return Result.success()
        }

        when (PreferenceManager(applicationContext).eventReminderInterval) {
            PreferenceManager.EVENT_REMINDER_INTERVAL_15_MINUTES -> event.schedule = event.schedule!!.minusMinutes(15)
            PreferenceManager.EVENT_REMINDER_INTERVAL_30_MINUTES -> event.schedule = event.schedule!!.minusMinutes(30)
            PreferenceManager.EVENT_REMINDER_INTERVAL_60_MINUTES -> event.schedule = event.schedule!!.minusMinutes(60)
        }

        if (currentTime.isBefore(event.schedule!!)) {
            val delay = Duration(currentTime.toDateTime(DateTimeZone.UTC),
                event.schedule!!.toDateTime(DateTimeZone.UTC))
            notificationRequest.setInitialDelay(delay.standardMinutes, TimeUnit.MINUTES)
        }

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(event.eventID,
            ExistingWorkPolicy.REPLACE, notificationRequest.build())

        return Result.success()
    }
}