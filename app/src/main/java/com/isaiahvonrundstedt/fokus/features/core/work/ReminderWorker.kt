package com.isaiahvonrundstedt.fokus.features.core.work

import android.app.Application
import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.repository.HistoryRepository
import com.isaiahvonrundstedt.fokus.features.history.History
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.util.concurrent.TimeUnit

// This worker's function is to only show reminders
// based on the frequency the user has selected; daily or every weekends
// This will show a reminders for pending tasks.
class ReminderWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var database = AppDatabase.getInstance(applicationContext)
    private val dataStore by lazy {
        HistoryRepository.getInstance(context.applicationContext as Application)
    }

    private fun scheduleNextReminder() {
        Scheduler()
            .removePrevious(true)
            .setTargetTime(
                PreferenceManager(
                    applicationContext
                ).reminderTime?.toDateTimeToday())
            .schedule(applicationContext)
    }

    override suspend fun doWork(): Result {
        val currentTime = LocalDateTime.now()
        scheduleNextReminder()

        val taskSize: Int = database!!.tasks().fetchCount()
        var history: History? = null
        if (taskSize > 0) {
            history = History().apply {
                title = String.format(applicationContext.getString(R.string.notification_pending_tasks_title),
                    taskSize)
                content = applicationContext.getString(R.string.notification_pending_tasks_summary)
                type = History.TYPE_GENERIC
                dateTimeTriggered = DateTime.now()
            }
        }

        if (PreferenceManager(
                applicationContext
            ).reminderFrequency == PreferenceManager.DURATION_WEEKENDS
            && !(currentTime.dayOfWeek == DateTimeConstants.SATURDAY
                    || currentTime.dayOfWeek == DateTimeConstants.SUNDAY))
            return Result.success()

        if (history != null) {
            dataStore.insert(history)
            sendNotification(history)
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

            val reminderTime = PreferenceManager(
                context
            ).reminderTime
            val executionTime = if (DateTime.now().isBefore(reminderTime?.toDateTimeToday()))
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay()
                    .plusHours(reminderTime?.hourOfDay ?: 8)
                    .plusMinutes(reminderTime?.minuteOfHour ?: 30)
                    .plusMinutes(1))
            else
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay()
                    .plusDays(1)
                    .plusHours(reminderTime?.hourOfDay ?: 8)
                    .plusMinutes(reminderTime?.minuteOfHour ?: 30))

            if (removePrevious) workManager.cancelAllWorkByTag(ReminderWorker::class.java.simpleName)

            val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInitialDelay(executionTime.standardMinutes, TimeUnit.MINUTES)
                .addTag(ReminderWorker::class.java.simpleName)
                .build()
            workManager.enqueue(request)
        }
    }
}