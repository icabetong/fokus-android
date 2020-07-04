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
    private var tasks = database?.tasks()
    private var histories = database?.histories()

    override suspend fun doWork(): Result {
        val currentTime = LocalDateTime.now()
        reschedule(applicationContext)

        val taskSize: Int = tasks?.fetchCount() ?: 0
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

        if (preferenceManager.reminderFrequency == PreferenceManager.DURATION_WEEKENDS
            && !(currentTime.dayOfWeek == DateTimeConstants.SATURDAY
                    || currentTime.dayOfWeek == DateTimeConstants.SUNDAY))
            return Result.success()

        if (history != null) {
            histories?.insert(history)
            sendNotification(history)
        }

        return Result.success()
    }

    companion object {

        fun reschedule(context: Context) {
            val manager = WorkManager.getInstance(context)
            val preferences = PreferenceManager(context)

            manager.cancelAllWorkByTag(this::class.java.simpleName)

            val reminderTime = preferences.reminderTime?.toDateTimeToday()
            val executionTime = DateTime.now().withTimeAtStartOfDay()
            if (reminderTime?.isAfterNow == true)
                executionTime.plusDays(1)

            executionTime.plusHours(reminderTime?.hourOfDay ?: 8)
                .plusMinutes(reminderTime?.minuteOfHour ?: 30)

            val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInitialDelay(Duration(DateTime.now(), executionTime).standardMinutes,
                    TimeUnit.MINUTES)
                .addTag(this::class.java.simpleName)
                .build()
            manager.enqueue(request)
        }
    }
}