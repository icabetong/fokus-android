package com.isaiahvonrundstedt.fokus.features.core.work.subject

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.core.work.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.history.History
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

class ClassNotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val schedule = convertDataToSchedule(inputData)
        val history = History().apply {
            title = schedule.subject
            content = schedule.format(applicationContext)
            type = History.TYPE_CLASS
            isPersistent = false
            data = schedule.scheduleID
        }

        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        request.setInputData(convertHistoryToData(history))

        schedule.getDaysAsList().forEach {
            var triggerTime: DateTime = Schedule.getNextWeekDay(it)

            when (preferenceManager.subjectReminderInterval) {
                PreferenceManager.SUBJECT_REMINDER_INTERVAL_5_MINUTES ->
                    triggerTime = triggerTime.minusMinutes(5)
                PreferenceManager.SUBJECT_REMINDER_INTERVAL_15_MINUTES ->
                    triggerTime = triggerTime.minusMinutes(15)
                PreferenceManager.SUBJECT_REMINDER_INTERVAL_30_MINUTES ->
                    triggerTime = triggerTime.minusMinutes(30)
            }

            if (triggerTime.isAfterNow)
                request.setInitialDelay(Duration(DateTime.now(), triggerTime).standardMinutes,
                    TimeUnit.MINUTES)

            workManager.enqueueUniqueWork(schedule.scheduleID, ExistingWorkPolicy.REPLACE,
                request.build())
        }
        return Result.success()
    }
}