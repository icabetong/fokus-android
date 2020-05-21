package com.isaiahvonrundstedt.fokus.features.core.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

class EventNotificationWorker(private var context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val currentTime = DateTime.now()

        val event = convertDataToEvent(inputData)
        val notification = Notification().apply {
            title = event.name
            content = event.formatSchedule(context)
            type = Notification.typeEventAlert
            data = event.id
        }

        when (PreferenceManager(context).dueInterval) {
            PreferenceManager.dueDelayHour -> event.schedule = event.schedule!!.minusHours(1)
            PreferenceManager.dueDelayThreeHours -> event.schedule = event.schedule!!.minusHours(3)
            PreferenceManager.dueDelayDay -> event.schedule = event.schedule!!.minusHours(24)
        }
        val notificationRequest = OneTimeWorkRequestBuilder<CoreNotificationWorker>()
        if (currentTime.isBefore(event.schedule)) {
            val delay = Duration(currentTime.toDateTime(DateTimeZone.UTC),
                event.schedule!!.toDateTime(DateTimeZone.UTC))
            notificationRequest.setInitialDelay(delay.standardMinutes, TimeUnit.MINUTES)
        }
        notificationRequest.setInputData(convertNotificationToData(notification))

        WorkManager.getInstance(context).enqueueUniqueWork(event.id, ExistingWorkPolicy.REPLACE,
            notificationRequest.build())

        return Result.success()
    }
}