package com.isaiahvonrundstedt.fokus.features.notifications.task

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

// This worker's function is to schedule the fokus worker
// for the task minus the interval.
@HiltWorker
class TaskNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val task = convertDataToTask(inputData)
        val resID = if (task.isDueToday()) R.string.due_today_at else R.string.due_at
        val log = Log().apply {
            title = task.name
            content = if (task.hasDueDate()) String.format(
                applicationContext.getString(resID),
                task.dueDate?.format(DateTimeConverter.getDateTimeFormatter(applicationContext))
            )
            else null
            type = Log.TYPE_TASK
            isImportant = task.isImportant
            data = task.taskID
        }

        if (!task.isImportant && !task.hasDueDate())
            return Result.success()

        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(convertLogToData(log))

        if (log.isImportant) {
            workManager.enqueueUniqueWork(
                task.taskID, ExistingWorkPolicy.REPLACE,
                request.build()
            )
            return Result.success()
        }

        var executionTime = task.dueDate
        when (preferenceManager.taskReminderInterval) {
            PreferenceManager.TASK_REMINDER_INTERVAL_1_HOUR ->
                executionTime = task.dueDate?.minusHours(1)
            PreferenceManager.TASK_REMINDER_INTERVAL_3_HOURS ->
                executionTime = task.dueDate?.minusHours(3)
            PreferenceManager.TASK_REMINDER_INTERVAL_24_HOURS ->
                executionTime = task.dueDate?.minusHours(24)
        }

        if (executionTime?.isAfterNow() == true)
            request.setInitialDelay(
                Duration.between(ZonedDateTime.now(), executionTime).toMinutes(),
                TimeUnit.MINUTES
            )

        workManager.enqueueUniqueWork(
            task.taskID, ExistingWorkPolicy.REPLACE,
            request.build()
        )

        return Result.success()
    }
}