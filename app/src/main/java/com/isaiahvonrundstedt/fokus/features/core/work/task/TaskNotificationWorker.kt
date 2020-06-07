package com.isaiahvonrundstedt.fokus.features.core.work.task

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.work.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit

// This worker's function is to schedule the fokus worker
// for the task minus the interval.
class TaskNotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val currentTime = DateTime.now()

        val task = convertDataToTask(inputData)
        val resID = if (task.isDueToday()) R.string.due_today_at else R.string.due_tomorrow_at
        val notification = Notification().apply {
            title = task.name
            content = String.format(applicationContext.getString(resID),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(task.dueDate!!))
            type = Notification.typeTaskReminder
            isPersistent = task.isImportant
            data = task.taskID
        }

        val notificationRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        notificationRequest.setInputData(convertNotificationToData(notification))

        if (notification.isPersistent) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(task.taskID,
                ExistingWorkPolicy.REPLACE, notificationRequest.build())
            return Result.success()
        }

        when (PreferenceManager(applicationContext).taskReminderInterval) {
            PreferenceManager.taskReminderIntervalHour -> task.dueDate = task.dueDate!!.minusHours(1)
            PreferenceManager.taskReminderIntervalThreeHours -> task.dueDate = task.dueDate!!.minusHours(3)
            PreferenceManager.taskReminderIntervalDay -> task.dueDate = task.dueDate!!.minusHours(24)
        }

        if (currentTime.isBefore(task.dueDate!!)) {
            val delay = Duration(currentTime.toDateTime(DateTimeZone.UTC),
                task.dueDate!!.toDateTime(DateTimeZone.UTC))
            notificationRequest.setInitialDelay(delay.standardMinutes, TimeUnit.MINUTES)
        }

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(task.taskID,
            ExistingWorkPolicy.REPLACE, notificationRequest.build())

        return Result.success()
    }

}