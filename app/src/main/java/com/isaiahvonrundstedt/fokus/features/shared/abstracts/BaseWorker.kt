package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.core.service.NotificationActionService
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.task.Task

abstract class BaseWorker(private var context: Context, workerParameters: WorkerParameters)
    : CoroutineWorker(context, workerParameters) {

    companion object {
        const val eventNotificationID = 14
        const val eventNotificationChannelID = "eventChannelID"
        const val taskNotificationID = 27
        const val taskNotificationChannelID = "taskChannelID"
        const val genericNotificationID = 38
        const val genericNotificationChannelID = "genericChannelID"

        private const val extraNotificationID = "id"
        private const val extraNotificationTitle = "title"
        private const val extraNotificationContent = "content"
        private const val extraNotificationType = "type"
        private const val extraNotificationData = "data"

        private const val extraTaskID = "taskID"
        private const val extraTaskName = "name"
        private const val extraTaskNotes = "notes"
        private const val extraTaskSubjectID = "subjectID"
        private const val extraTaskDue = "due"

        private const val extraEventID = "eventID"
        private const val extraEventName = "name"
        private const val extraEventNotes = "notes"
        private const val extraEventLocation = "location"
        private const val extraEventSchedule = "schedule"

        fun convertNotificationToData(notification: Notification): Data {
            return Data.Builder().apply {
                putString(extraNotificationID, notification.id)
                putString(extraNotificationTitle, notification.title)
                putString(extraNotificationContent, notification.content)
                putString(extraNotificationData, notification.data)
                putInt(extraNotificationType, notification.type)
            }.build()
        }

        fun convertTaskToData(task: Task): Data {
            return Data.Builder().apply {
                putString(extraTaskID, task.taskID)
                putString(extraTaskName, task.name)
                putString(extraTaskNotes, task.notes)
                putString(extraTaskSubjectID, task.subjectID)
                putString(extraTaskDue, DateTimeConverter.fromDateTime(task.dueDate!!))
            }.build()
        }

        fun convertEventToData(event: Event): Data {
            return Data.Builder().apply {
                putString(extraEventID, event.eventID)
                putString(extraEventName, event.name)
                putString(extraEventNotes, event.notes)
                putString(extraEventLocation, event.location)
                putString(extraEventSchedule, DateTimeConverter.fromDateTime(event.schedule!!))
            }.build()
        }

        fun convertDataToNotification(workerData: Data): Notification {
            return Notification().apply {
                id = workerData.getString(extraNotificationID)!!
                title = workerData.getString(extraNotificationTitle)
                content = workerData.getString(extraNotificationContent)
                data = workerData.getString(extraNotificationData)
                type = workerData.getInt(extraNotificationType, Notification.typeGeneric)
            }
        }

        fun convertDataToTask(workerData: Data): Task {
            return Task().apply {
                taskID = workerData.getString(extraTaskID)!!
                name = workerData.getString(extraTaskName)
                notes = workerData.getString(extraTaskNotes)
                subjectID = workerData.getString(extraTaskSubjectID)
                dueDate = DateTimeConverter.toDateTime(workerData.getString(extraTaskDue)!!)
            }
        }

        fun convertDataToEvent(workerData: Data): Event {
            return Event().apply {
                eventID = workerData.getString(extraEventID)!!
                name = workerData.getString(extraEventName)
                notes = workerData.getString(extraEventNotes)
                location = workerData.getString(extraEventLocation)
                schedule = DateTimeConverter.toDateTime(workerData.getString(extraEventSchedule))
            }
        }
    }

    protected fun sendNotification(notification: Notification) {
        createNotificationChannel(notification.type)
        if (notification.type == Notification.typeTaskReminder)
            manager.notify(taskNotificationID, createTaskNotification(notification))
        else if (notification.type == Notification.typeEventReminder)
            manager.notify(eventNotificationID, createNotification(notification))
        else manager.notify(genericNotificationID, createNotification(notification))
    }

    private fun createNotificationChannel(type: Int) {
        with(NotificationManagerCompat.from(context)) {
            val id = when (type) {
                Notification.typeTaskReminder -> taskNotificationChannelID
                Notification.typeEventReminder -> eventNotificationChannelID
                else -> genericNotificationChannelID
            }
            val resID = when (type) {
                Notification.typeTaskReminder -> R.string.notification_channel_task_reminders
                Notification.typeEventReminder -> R.string.notification_channel_event_reminders
                else -> R.string.notification_channel_generic
            }

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            if (getNotificationChannel(id) == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    manager.createNotificationChannel(
                        NotificationChannel(id, context.getString(resID),
                            NotificationManager.IMPORTANCE_HIGH).apply {
                                setSound(notificationSoundUri, attributes)
                            })
            }
        }
    }

    private fun createTaskNotification(notification: Notification): android.app.Notification {
        return NotificationCompat.Builder(context, taskNotificationChannelID).apply {
            setSound(notificationSoundUri)
            setSmallIcon(R.drawable.ic_brand_black)
            setContentIntent(contentIntent)
            setContentTitle(notification.title)
            setContentText(notification.content)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            addAction(R.drawable.ic_check_white, context.getString(R.string.button_mark_as_finished),
                createPendingIntent(NotificationActionService.finishID, NotificationActionService.actionFinished,
                    NotificationActionService.extraTaskID, notification.data!!))
        }.build()
    }

    private fun createNotification(notification: Notification?): android.app.Notification {
        return NotificationCompat.Builder(context, taskNotificationChannelID).apply {
            setSound(notificationSoundUri)
            setSmallIcon(R.drawable.ic_brand_black)
            setContentIntent(contentIntent)
            setContentTitle(notification?.title)
            setContentText(notification?.content)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
        }.build()
    }

    private fun createPendingIntent(id: Int, action: String, extra: String, data: String): PendingIntent {
        return PendingIntent.getService(context, id,
            Intent(context, NotificationActionService::class.java).apply {
                setAction(action)
                putExtra(extra, data)
            }, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val manager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val contentIntent: PendingIntent
        get() {
            return PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        }

    private val notificationSoundUri: Uri
        get() {
            return PreferenceManager(applicationContext).let {
                if (it.customSoundEnabled) it.soundUri
                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }
}