package com.isaiahvonrundstedt.fokus.features.core.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit

class DeadlineScheduler(private var context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val currentTime = LocalDateTime.now()

        val task = convertDataToTask(inputData)
        val resID = if (task.isDueToday()) R.string.due_today_at else R.string.due_tomorrow_at
        val notification = Notification().apply {
            title = task.name
            content = String.format(context.getString(resID),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(task.dueDate!!))
            type = Notification.typeDueAlert
            data = task.taskID
        }

        when (PreferenceManager(context).dueInterval) {
            PreferenceManager.dueDelayHour -> task.dueDate = task.dueDate!!.minusHours(1)
            PreferenceManager.dueDelayThreeHours -> task.dueDate = task.dueDate!!.minusHours(3)
            PreferenceManager.dueDelayDay -> task.dueDate = task.dueDate!!.minusHours(24)
        }

        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        if (currentTime.isBefore(task.dueDate!!)) {
            val delay = Duration(currentTime.toDateTime(DateTimeZone.UTC),
                task.dueDate!!.toDateTime(DateTimeZone.UTC))
            notificationRequest.setInitialDelay(delay.standardMinutes, TimeUnit.MINUTES)
        }
        notificationRequest.setInputData(convertNotificationToData(notification))

        WorkManager.getInstance(context).enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
            notificationRequest.build())

        return Result.success()
    }

}