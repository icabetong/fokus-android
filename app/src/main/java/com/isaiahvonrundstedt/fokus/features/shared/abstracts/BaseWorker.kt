package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.components.service.NotificationActionService
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.utils.AppNotificationManager
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.task.Task

abstract class BaseWorker(context: Context, workerParameters: WorkerParameters)
    : CoroutineWorker(context, workerParameters) {

    companion object {
        const val NOTIFICATION_ID_EVENT = 14
        const val NOTIFICATION_TAG_EVENT = "com:isaiahvonrundstedt:fokus:event"
        const val NOTIFICATION_CHANNEL_ID_EVENT = "channel:event"
        const val NOTIFICATION_ID_TASK = 27
        const val NOTIFICATION_TAG_TASK = "com:isaiahvonrundstedt:fokus:task"
        const val NOTIFICATION_CHANNEL_ID_TASK = "channel:task"
        const val NOTIFICATION_ID_GENERIC = 38
        const val NOTIFICATION_TAG_GENERIC = "com:isaiahvonrundstedt:fokus:generic"
        const val NOTIFICATION_CHANNEL_ID_GENERIC = "channel:generic"

        private const val EXTRA_LOG_ID = "extra:history:id"
        private const val EXTRA_LOG_TITLE = "extra:history:title"
        private const val EXTRA_LOG_CONTENT = "extra:history:content"
        private const val EXTRA_LOG_TYPE = "extra:history:type"
        private const val EXTRA_LOG_DATA = "extra:history:data"
        private const val EXTRA_LOG_PERSISTENCE = "extra:history:persistence"

        private const val EXTRA_TASK_ID = "extra:task:id"
        private const val EXTRA_TASK_NAME = "extra:task:name"
        private const val EXTRA_TASK_NOTES = "extra:task:notes"
        private const val EXTRA_TASK_SUBJECT = "extra:task:subject"
        private const val EXTRA_TASK_DUE = "extra:task:due"
        private const val EXTRA_TASK_IMPORTANCE = "extra:task:importance"

        private const val EXTRA_EVENT_ID = "extra:event:id"
        private const val EXTRA_EVENT_NAME = "extra:event:name"
        private const val EXTRA_EVENT_NOTES = "extra:event:notes"
        private const val EXTRA_EVENT_LOCATION = "extra:event:location"
        private const val EXTRA_EVENT_SCHEDULE = "extra:event:schedule"
        private const val EVENT_EVENT_IMPORTANCE = "extra:event:isImportant"

        private const val EXTRA_SCHEDULE_ID = "extra:schedule:id"
        private const val EXTRA_SCHEDULE_DAY_OF_WEEK = "extra:schedule:day"
        private const val EXTRA_SCHEDULE_START_TIME = "extra:schedule:start:time"
        private const val EXTRA_SCHEDULE_END_TIME = "extra:schedule:end:time"
        private const val EXTRA_SCHEDULE_SUBJECT = "extra:schedule:subject"

        fun convertLogToData(log: Log): Data {
            return Data.Builder().apply {
                putString(EXTRA_LOG_ID, log.logID)
                putString(EXTRA_LOG_TITLE, log.title)
                putString(EXTRA_LOG_CONTENT, log.content)
                putString(EXTRA_LOG_DATA, log.data)
                putInt(EXTRA_LOG_TYPE, log.type)
                putBoolean(EXTRA_LOG_PERSISTENCE, log.isImportant)
            }.build()
        }

        fun convertTaskToData(task: Task): Data {
            return Data.Builder().apply {
                putString(EXTRA_TASK_ID, task.taskID)
                putString(EXTRA_TASK_NAME, task.name)
                putString(EXTRA_TASK_NOTES, task.notes)
                putString(EXTRA_TASK_SUBJECT, task.subject)
                putString(EXTRA_TASK_DUE, DateTimeConverter.fromDateTime(task.dueDate))
                putBoolean(EXTRA_TASK_IMPORTANCE, task.isImportant)
            }.build()
        }

        fun convertEventToData(event: Event): Data {
            return Data.Builder().apply {
                putString(EXTRA_EVENT_ID, event.eventID)
                putString(EXTRA_EVENT_NAME, event.name)
                putString(EXTRA_EVENT_NOTES, event.notes)
                putString(EXTRA_EVENT_LOCATION, event.location)
                putString(EXTRA_EVENT_SCHEDULE, DateTimeConverter.fromDateTime(event.schedule!!))
                putBoolean(EVENT_EVENT_IMPORTANCE, event.isImportant)
            }.build()
        }

        fun convertScheduleToData(schedule: Schedule): Data {
            return Data.Builder().apply {
                putString(EXTRA_SCHEDULE_ID, schedule.scheduleID)
                putString(EXTRA_SCHEDULE_SUBJECT, schedule.subject)
                putString(EXTRA_SCHEDULE_START_TIME, DateTimeConverter.fromTime(schedule.startTime))
                putString(EXTRA_SCHEDULE_END_TIME, DateTimeConverter.fromTime(schedule.endTime))
                putInt(EXTRA_SCHEDULE_DAY_OF_WEEK, schedule.daysOfWeek)
            }.build()
        }

        fun convertDataToLog(workerData: Data): Log {
            return Log().apply {
                workerData.getString(EXTRA_LOG_ID)?.let { logID = it }
                title = workerData.getString(EXTRA_LOG_TITLE)
                content = workerData.getString(EXTRA_LOG_CONTENT)
                data = workerData.getString(EXTRA_LOG_DATA)
                type = workerData.getInt(EXTRA_LOG_TYPE, Log.TYPE_GENERIC)
                isImportant = workerData.getBoolean(EXTRA_LOG_PERSISTENCE, false)
            }
        }

        fun convertDataToTask(workerData: Data): Task {
            return Task().apply {
                workerData.getString(EXTRA_TASK_ID)?.let { taskID = it }
                name = workerData.getString(EXTRA_TASK_NAME)
                notes = workerData.getString(EXTRA_TASK_NOTES)
                subject = workerData.getString(EXTRA_TASK_SUBJECT)
                isImportant = workerData.getBoolean(EXTRA_TASK_IMPORTANCE, false)
                dueDate = DateTimeConverter.toDateTime(workerData.getString(EXTRA_TASK_DUE))
            }
        }

        fun convertDataToEvent(workerData: Data): Event {
            return Event().apply {
                workerData.getString(EXTRA_EVENT_ID)?.let { eventID = it }
                name = workerData.getString(EXTRA_EVENT_NAME)
                notes = workerData.getString(EXTRA_EVENT_NOTES)
                location = workerData.getString(EXTRA_EVENT_LOCATION)
                schedule = DateTimeConverter.toDateTime(workerData.getString(EXTRA_EVENT_SCHEDULE))
                isImportant = workerData.getBoolean(EVENT_EVENT_IMPORTANCE, false)
            }
        }

        fun convertDataToSchedule(workerData: Data): Schedule {
            return Schedule().apply {
                workerData.getString(EXTRA_SCHEDULE_ID)?.let { scheduleID = it }
                subject = workerData.getString(EXTRA_SCHEDULE_SUBJECT)
                startTime = DateTimeConverter.toTime(workerData.getString(EXTRA_SCHEDULE_START_TIME))
                endTime = DateTimeConverter.toTime(workerData.getString(EXTRA_SCHEDULE_END_TIME))
                daysOfWeek = workerData.getInt(EXTRA_SCHEDULE_DAY_OF_WEEK, 0)
            }
        }

    }

    protected fun sendNotification(log: Log, @Nullable tag: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(log.type)

        if (log.type == Log.TYPE_TASK) {
            val intent = PendingIntent.getService(applicationContext, NotificationActionService.finishID,
                Intent(applicationContext, NotificationActionService::class.java).apply {
                    putExtra(NotificationActionService.EXTRA_TASK_ID, log.data)
                    putExtra(NotificationActionService.EXTRA_IS_PERSISTENT, log.isImportant)
                    action = NotificationActionService.ACTION_FINISHED
                }, PendingIntent.FLAG_UPDATE_CURRENT)

            notificationManager.notify(tag ?: NOTIFICATION_TAG_TASK, NOTIFICATION_ID_TASK,
                createNotification(log, NOTIFICATION_CHANNEL_ID_TASK,
                    NotificationCompat.Action(R.drawable.ic_outline_done_24,
                        applicationContext.getString(R.string.button_mark_as_finished), intent)))
        } else if (log.type == Log.TYPE_EVENT)
            notificationManager.notify(tag ?: NOTIFICATION_TAG_EVENT, NOTIFICATION_ID_EVENT,
                createNotification(log, NOTIFICATION_CHANNEL_ID_EVENT))
        else notificationManager.notify(tag ?: NOTIFICATION_TAG_GENERIC, NOTIFICATION_ID_GENERIC,
            createNotification(log, NOTIFICATION_CHANNEL_ID_GENERIC))
    }

    @RequiresApi(26)
    private fun createNotificationChannel(type: Int) {
        val id = when (type) {
            Log.TYPE_TASK -> AppNotificationManager.CHANNEL_ID_TASK
            Log.TYPE_EVENT -> AppNotificationManager.CHANNEL_ID_EVENT
            else -> AppNotificationManager.CHANNEL_ID_GENERIC
        }
        AppNotificationManager(applicationContext).create(id)
    }

    private fun createNotification(log: Log?, id: String,
                                   @Nullable action: NotificationCompat.Action? = null): Notification {
        return NotificationCompat.Builder(applicationContext, id).apply {
            setSound(notificationSoundUri)
            setSmallIcon(R.drawable.ic_outline_done_all_24)
            setContentIntent(contentIntent)
            setContentTitle(log?.title)
            setContentText(log?.content)
            setOngoing(log?.isImportant == true)
            if (action != null) addAction(action)
            color = ContextCompat.getColor(applicationContext, R.color.color_primary)
        }.build()
    }

    private val notificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    protected val preferenceManager by lazy {
        PreferenceManager(applicationContext)
    }

    protected val workManager by lazy {
        WorkManager.getInstance(applicationContext)
    }

    private val contentIntent: PendingIntent
        get() {
            return PendingIntent.getActivity(applicationContext, 0,
                Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        }

    private val notificationSoundUri: Uri
        get() {
            return PreferenceManager(applicationContext).let {
                if (it.customSoundEnabled) it.customSoundUri
                else PreferenceManager.DEFAULT_SOUND_URI
            }
        }

}