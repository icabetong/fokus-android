package com.isaiahvonrundstedt.fokus.features.notifications.subject

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationWorker
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

class ClassNotificationWorker(context: Context, workerParameters: WorkerParameters)
    : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val schedule = convertDataToSchedule(inputData)
        val history = Log().apply {
            title = schedule.subject
            content = schedule.format(applicationContext)
            type = Log.TYPE_CLASS
            isImportant = false
            data = schedule.scheduleID
        }

        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        request.setInputData(convertLogToData(history))

        schedule.getDaysAsList().forEach {
            var triggerTime: DateTime = Schedule.getNextWeekDay(it, schedule.startTime)

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

            workManager.enqueueUniqueWork(schedule.scheduleID, ExistingWorkPolicy.APPEND,
                request.build())
            reschedule(schedule.scheduleID, inputData, triggerTime)
        }
        return Result.success()
    }

    private fun reschedule(tag: String, data: Data, triggerTime: DateTime) {
        val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
            .setInputData(data)
            .setInitialDelay(Duration(triggerTime, triggerTime.plusWeeks(1)).standardMinutes,
                TimeUnit.MINUTES)
        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.APPEND,
            request.build())
    }
}