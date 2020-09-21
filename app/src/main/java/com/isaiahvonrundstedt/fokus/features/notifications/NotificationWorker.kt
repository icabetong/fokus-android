package com.isaiahvonrundstedt.fokus.features.notifications

import android.content.Context
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import java.time.ZonedDateTime

// This worker fetches the fokus passed by various
// worker classes. It's primary purpose is to only trigger
// and to show the fokus. Also to insert the fokus
// object to the database.
class NotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    private val logRepository by lazy { LogRepository.getInstance(applicationContext) }

    override suspend fun doWork(): Result {
        val log: Log = convertDataToLog(inputData)
        log.dateTimeTriggered = ZonedDateTime.now()

        logRepository.insert(log)
        if (log.isImportant)
            sendNotification(log, log.data)
        else sendNotification(log)

        return Result.success()
    }
}