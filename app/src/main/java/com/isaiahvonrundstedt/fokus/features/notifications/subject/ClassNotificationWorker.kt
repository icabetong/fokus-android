package com.isaiahvonrundstedt.fokus.features.notifications.subject

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class ClassNotificationWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val schedule = convertDataToSchedule(inputData)
        val log = Log().apply {
            title = schedule.subject
            content = schedule.format(applicationContext)
            type = Log.TYPE_CLASS
            isImportant = false
            data = schedule.scheduleID
        }

        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        request.setInputData(convertLogToData(log))

        schedule.getDaysAsList().forEach {
            var triggerTime = schedule.startTime?.let { time -> Schedule.getNextWeekDay(it, time) }

            when (preferenceManager.subjectReminderInterval) {
                PreferenceManager.SUBJECT_REMINDER_INTERVAL_5_MINUTES ->
                    triggerTime = triggerTime?.minusMinutes(5)
                PreferenceManager.SUBJECT_REMINDER_INTERVAL_15_MINUTES ->
                    triggerTime = triggerTime?.minusMinutes(15)
                PreferenceManager.SUBJECT_REMINDER_INTERVAL_30_MINUTES ->
                    triggerTime = triggerTime?.minusMinutes(30)
            }

            if (triggerTime?.isAfterNow() == true)
                request.setInitialDelay(Duration.between(ZonedDateTime.now(), triggerTime).toMinutes(),
                    TimeUnit.MINUTES)

            workManager.enqueueUniqueWork(schedule.scheduleID, ExistingWorkPolicy.APPEND,
                request.build())
            reschedule(schedule.scheduleID, inputData, triggerTime)
        }

        return Result.success()
    }

    private fun reschedule(tag: String, data: Data, triggerTime: ZonedDateTime?) {
        val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
            .setInputData(data)
            .setInitialDelay(Duration.between(ZonedDateTime.now(), triggerTime).toMinutes(),
                TimeUnit.MINUTES)
        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.APPEND,
            request.build())
    }
}