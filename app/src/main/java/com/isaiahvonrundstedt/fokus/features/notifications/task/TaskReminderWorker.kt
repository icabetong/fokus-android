package com.isaiahvonrundstedt.fokus.features.notifications.task

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.*
import java.util.concurrent.TimeUnit

// This worker's function is to only show reminders
// based on the frequency the user has selected; daily or every weekends
// This will show a reminders for pending tasks.
@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val logRepository: LogRepository,
    private val preferenceManager: PreferenceManager,
    private val notificationManager: NotificationManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val currentTime = ZonedDateTime.now()
        reschedule(applicationContext)

        val taskSize: Int = taskRepository.fetchCount()
        var log: Log? = null
        if (taskSize > 0) {
            log = Log().apply {
                title = String.format(
                    applicationContext.getString(R.string.notification_pending_tasks_title),
                    taskSize
                )
                content = applicationContext.getString(R.string.notification_pending_tasks_summary)
                type = Log.TYPE_TASK
                dateTimeTriggered = ZonedDateTime.now()
            }
        }

        if (preferenceManager.reminderFrequency == PreferenceManager.DURATION_WEEKENDS
            && !(currentTime.dayOfWeek == DayOfWeek.SUNDAY
                    || currentTime.dayOfWeek == DayOfWeek.SATURDAY)
        )
            return Result.success()

        if (log != null) {
            logRepository.insert(log)
            sendNotification(log, notificationManager)
        }

        return Result.success()
    }

    companion object {
        fun reschedule(context: Context) {
            val manager = WorkManager.getInstance(context)
            val preferences = PreferenceManager(context)

            val reminderTime: ZonedDateTime? = ZonedDateTime.of(
                LocalDate.now(),
                preferences.reminderTime, ZoneId.systemDefault()
            )

            val executionTime: ZonedDateTime? = if (ZonedDateTime.now().isBefore(reminderTime))
                LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .plusHours((reminderTime?.hour ?: 8).toLong())
                    .plusMinutes((reminderTime?.minute ?: 30).toLong())
                    .plusMinutes(1)
            else
                LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .plusDays(1)
                    .plusHours((reminderTime?.hour ?: 8).toLong())
                    .plusMinutes((reminderTime?.minute ?: 30).toLong())

            manager.cancelAllWorkByTag(this::class.java.simpleName)

            val request = OneTimeWorkRequest.Builder(TaskReminderWorker::class.java)
                .setInitialDelay(
                    Duration.between(ZonedDateTime.now(), executionTime).toMinutes(),
                    TimeUnit.MINUTES
                )
                .addTag(this::class.java.simpleName)
                .build()
            manager.enqueue(request)
        }
    }
}