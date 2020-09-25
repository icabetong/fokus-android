package com.isaiahvonrundstedt.fokus.features.notifications.task

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.task.Task

// This worker's function is to reschedule all pending workers
// that is supposed to trigger at its due minus the interval
// This only triggers when the user has changed the fokus interval
// for tasks in the Settings
class TaskNotificationScheduler(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private val taskRepository by lazy { TaskRepository.getInstance(applicationContext) }

    override suspend fun doWork(): Result {
        val tasks: List<Task> = taskRepository.fetchCore()

        tasks.forEach { task ->
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(convertTaskToData(task))
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE, request)
        }

        return Result.success()
    }

}