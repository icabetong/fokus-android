package com.isaiahvonrundstedt.fokus.features.core.work

import android.app.Application
import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.repository.NotificationRepository
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.util.concurrent.TimeUnit

class TaskReminderWorker(private var context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var database = AppDatabase.getInstance(context)
    private val dataStore by lazy {
        NotificationRepository.getInstance(context.applicationContext as Application)
    }

    private fun scheduleNextReminder() {
        Scheduler()
            .removePrevious(true)
            .setTargetTime(PreferenceManager(context).reminderTime?.toDateTimeToday())
            .schedule(context)
    }

    override suspend fun doWork(): Result {
        val currentTime = LocalDateTime.now()
        scheduleNextReminder()

        val taskSize: Int = database!!.tasks().fetchCount()
        var notification: Notification? = null
        if (taskSize > 0) {
            notification = Notification().apply {
                title = String.format(context.getString(R.string.notification_pending_tasks_title),
                    taskSize)
                content = context.getString(R.string.notification_pending_tasks_summary)
                type = Notification.typeGeneric
                dateTimeTriggered = DateTime.now()
            }
        }

        if (PreferenceManager(context).reminderFrequency == PreferenceManager.durationWeekends
            && !(currentTime.dayOfWeek == DateTimeConstants.SATURDAY
                    || currentTime.dayOfWeek == DateTimeConstants.SUNDAY))
            return Result.success()

        if (notification != null) {
            dataStore.insert(notification)
            sendNotification(notification)
        }

        return Result.success()
    }

    class Scheduler {
        private var removePrevious: Boolean = true
        private var targetTime: DateTime = DateTime.now()

        fun removePrevious(removePrevious: Boolean): Scheduler {
            this.removePrevious = removePrevious
            return this
        }

        fun setTargetTime(targetTime: DateTime?): Scheduler {
            this.targetTime = targetTime ?: DateTime.now().withHourOfDay(8).withMinuteOfHour(30)
            return this
        }

        fun schedule(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val reminderTime = PreferenceManager(context).reminderTime
            val executionTime = if (DateTime.now().isBefore(reminderTime?.toDateTimeToday()))
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay()
                    .plusHours(reminderTime?.hourOfDay ?: 8)
                    .plusMinutes(reminderTime?.minuteOfHour ?: 30))
            else
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay()
                    .plusDays(1)
                    .plusHours(reminderTime?.hourOfDay ?: 8)
                    .plusMinutes(reminderTime?.minuteOfHour ?: 30))

            if (removePrevious) workManager.cancelAllWorkByTag(TaskReminderWorker::class.java.simpleName)

            val request = OneTimeWorkRequest.Builder(TaskReminderWorker::class.java)
                .setInitialDelay(executionTime.standardMinutes, TimeUnit.MINUTES)
                .addTag(TaskReminderWorker::class.java.simpleName)
                .build()
            workManager.enqueue(request)
        }
    }
}