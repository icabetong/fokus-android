package com.isaiahvonrundstedt.fokus.features.core.work.task

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

// This worker's function is to reschedule all pending workers
// that is supposed to trigger at its due minus the interval
// This only triggers when the user has changed the fokus interval
// for tasks in the Settings
class TaskNotificationScheduler(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var tasks = AppDatabase.getInstance(applicationContext)?.tasks()

    override suspend fun doWork(): Result {
        val taskList = tasks?.fetch()
        taskList?.forEach { task ->
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(convertTaskToData(task))
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
                request)
        }

        return Result.success()
    }

}