package com.isaiahvonrundstedt.fokus.features.core.work.event

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.features.core.work.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

// This worker's function is to schedule the fokus worker
// for the event schedule minus the interval.
class EventNotificationWorker(private var context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val currentTime = DateTime.now()

        val event = convertDataToEvent(inputData)
        val notification = Notification().apply {
            title = event.name
            content = event.formatSchedule(context)
            type = Notification.typeEventReminder
            data = event.eventID
        }

        when (PreferenceManager(context).eventReminderInterval) {
            PreferenceManager.eventReminderIntervalQuarter -> event.schedule = event.schedule!!.minusMinutes(15)
            PreferenceManager.eventReminderIntervalHalf -> event.schedule = event.schedule!!.minusMinutes(30)
            PreferenceManager.eventReminderIntervalFull -> event.schedule = event.schedule!!.minusMinutes(60)
        }
        val notificationRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        if (currentTime.isBefore(event.schedule!!)) {
            val delay = Duration(currentTime.toDateTime(DateTimeZone.UTC),
                event.schedule!!.toDateTime(DateTimeZone.UTC))
            notificationRequest.setInitialDelay(delay.standardMinutes, TimeUnit.MINUTES)
        }
        notificationRequest.setInputData(convertNotificationToData(notification))

        WorkManager.getInstance(context).enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
            notificationRequest.build())

        return Result.success()
    }
}