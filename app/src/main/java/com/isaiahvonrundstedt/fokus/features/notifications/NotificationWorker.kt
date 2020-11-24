package com.isaiahvonrundstedt.fokus.features.notifications

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import java.time.ZonedDateTime

// This worker fetches the fokus passed by various
// worker classes. It's primary purpose is to only trigger
// and to show the fokus. Also to insert the fokus
// object to the database.
class NotificationWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: LogRepository
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val log: Log = convertDataToLog(inputData)
        log.dateTimeTriggered = ZonedDateTime.now()

        repository.insert(log)
        if (log.isImportant)
            sendNotification(log, log.data)
        else sendNotification(log)

        return Result.success()
    }
}