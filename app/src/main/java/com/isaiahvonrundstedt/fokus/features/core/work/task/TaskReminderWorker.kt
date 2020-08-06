package com.isaiahvonrundstedt.fokus.features.core.work.task

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

// This worker's function is to only show reminders
// based on the frequency the user has selected; daily or every weekends
// This will show a reminders for pending tasks.
class TaskReminderWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var database = AppDatabase.getInstance(applicationContext)
    private var tasks = database?.tasks()
    private var logs = database?.logs()

    override suspend fun doWork(): Result {
        val currentTime = DateTime.now()
        reschedule(applicationContext)

        val taskSize: Int = tasks?.fetchCount() ?: 0
        var log: Log? = null
        if (taskSize > 0) {
            log = Log().apply {
                title = String.format(applicationContext.getString(R.string.notification_pending_tasks_title),
                    taskSize)
                content = applicationContext.getString(R.string.notification_pending_tasks_summary)
                type = Log.TYPE_TASK
                dateTimeTriggered = DateTime.now()
            }
        }

        if (preferenceManager.reminderFrequency == PreferenceManager.DURATION_WEEKENDS
            && !(currentTime.dayOfWeek == DateTimeConstants.SATURDAY
                    || currentTime.dayOfWeek == DateTimeConstants.SUNDAY))
            return Result.success()

        if (log != null) {
            logs?.insert(log)
            sendNotification(log)
        }

        return Result.success()
    }

    companion object {

        fun reschedule(context: Context) {
            val manager = WorkManager.getInstance(context)
            val preferences = PreferenceManager(context)

            val reminderTime: DateTime? = preferences.reminderTime?.toDateTimeToday()
            val executionTime: DateTime? = if (DateTime.now().isBefore(reminderTime))
                DateTime.now().withTimeAtStartOfDay()
                    .plusHours(reminderTime?.hourOfDay ?: 8)
                    .plusMinutes(reminderTime?.minuteOfHour ?: 30)
                    .plusMinutes(1)
            else
                DateTime.now().withTimeAtStartOfDay()
                    .plusDays(1)
                    .plusHours(reminderTime?.hourOfDay ?: 8)
                    .plusMinutes(reminderTime?.minuteOfHour ?: 30)

            manager.cancelAllWorkByTag(this::class.java.simpleName)

            val request = OneTimeWorkRequest.Builder(TaskReminderWorker::class.java)
                .setInitialDelay(Duration(DateTime.now(), executionTime).standardMinutes,
                    TimeUnit.MINUTES)
                .addTag(this::class.java.simpleName)
                .build()
            manager.enqueue(request)
        }
    }
}