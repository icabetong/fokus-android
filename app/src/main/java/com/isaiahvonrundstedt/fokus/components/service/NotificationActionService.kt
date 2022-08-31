package com.isaiahvonrundstedt.fokus.components.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.features.core.worker.ActionWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

// This service function is to trigger the worker
// that will perform the appropriate action
// based on what the user tapped on the fokus
// Since PendingIntents can trigger Workers, this service
// acts like a middle man
class NotificationActionService : IntentService(SERVICE_NAME) {


    private val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        val taskID = intent?.getStringExtra(EXTRA_TASK_ID)
        val isPersistent = intent?.getBooleanExtra(EXTRA_IS_PERSISTENT, false) ?: false

        if (isPersistent)
            manager?.cancel(taskID, BaseWorker.NOTIFICATION_ID_TASK)
        else manager?.cancel(BaseWorker.NOTIFICATION_TAG_TASK, BaseWorker.NOTIFICATION_ID_TASK)

        val data = Data.Builder()
        data.putString(EXTRA_TASK_ID, taskID)
        if (intent?.action == ACTION_FINISHED)
            data.putString(EXTRA_ACTION, ACTION_FINISHED)

        val workRequest = OneTimeWorkRequest.Builder(ActionWorker::class.java)
            .setInputData(data.build())
            .addTag(ActionWorker::class.java.simpleName)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    companion object {
        const val SERVICE_NAME = "service:notification:actions"
        const val EXTRA_TASK_ID = "extra:taskID"
        const val EXTRA_IS_PERSISTENT = "extra:isPersistent"
        const val EXTRA_ACTION = "extra:action"

        const val ACTION_FINISHED = "action:finished"

        const val NOTIFICATION_ID_FINISH = 28
    }

}