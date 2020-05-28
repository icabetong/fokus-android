package com.isaiahvonrundstedt.fokus.features.core.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.features.core.work.ActionWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

// This service function is to trigger the worker
// that will perform the appropriate action
// based on what the user tapped on the fokus
// Since PendingIntents can trigger Workers, this service
// acts like a middle man
class NotificationActionService: IntentService(name) {

    companion object {
        const val name = "notificationActionService"
        const val extraTaskID = "taskID"
        const val extraAction = "action"
        const val actionFinished = "finished"

        const val finishID = 28
    }

    private val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onHandleIntent(intent: Intent?) {
        manager?.cancel(BaseWorker.taskNotificationID)

        val taskID = intent?.getStringExtra(extraTaskID)

        val data = Data.Builder()
        data.putString(extraTaskID, taskID)
        if (intent?.action == actionFinished)
            data.putString(extraAction, actionFinished)

        val workRequest = OneTimeWorkRequest.Builder(ActionWorker::class.java)
            .setInputData(data.build())
            .addTag(ActionWorker::class.java.simpleName)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

}