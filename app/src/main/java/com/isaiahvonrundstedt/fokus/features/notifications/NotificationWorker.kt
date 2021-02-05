package com.isaiahvonrundstedt.fokus.features.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.ZonedDateTime

// This worker fetches the fokus passed by various
// worker classes. It's primary purpose is to only trigger
// and to show the fokus. Also to insert the fokus
// object to the database.
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: LogRepository,
    private val notificationManager: NotificationManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val log: Log = convertDataToLog(inputData)
        log.dateTimeTriggered = ZonedDateTime.now()

        repository.insert(log)
        if (log.isImportant)
            sendNotification(log, notificationManager, log.data)
        else sendNotification(log, notificationManager)

        return Result.success()
    }
}