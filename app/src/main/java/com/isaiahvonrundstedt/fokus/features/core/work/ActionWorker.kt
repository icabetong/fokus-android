package com.isaiahvonrundstedt.fokus.features.core.work

import android.content.Context
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.core.service.NotificationActionService
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

// This worker's primary function perform the action
// at is triggered in the fokus such as 'Mark as Finished'
class ActionWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private var tasks = AppDatabase.getInstance(applicationContext)?.tasks()

    override suspend fun doWork(): Result {
        val action = inputData.getString(NotificationActionService.extraAction)
        val taskID = inputData.getString(NotificationActionService.extraTaskID)
        if (action.isNullOrBlank() || taskID.isNullOrBlank())
            return Result.success()

        if (action == NotificationActionService.actionFinished)
            tasks?.setFinished(taskID, 1)

        return Result.success()
    }

}