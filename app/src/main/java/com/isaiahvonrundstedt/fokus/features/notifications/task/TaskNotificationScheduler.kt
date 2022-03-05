package com.isaiahvonrundstedt.fokus.features.notifications.task

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.task.Task
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// This worker's function is to reschedule all pending workers
// that is supposed to trigger at its due minus the interval
// This only triggers when the user has changed the fokus interval
// for tasks in the Settings
@HiltWorker
class TaskNotificationScheduler @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: TaskRepository,
    private val workManager: WorkManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val tasks: List<Task> = repository.fetchCore()

        tasks.forEach { task ->
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(convertTaskToData(task))
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE, request)
        }

        return Result.success()
    }
}