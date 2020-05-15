package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskActivity

abstract class BaseWorker(private var context: Context, workerParameters: WorkerParameters)
    : CoroutineWorker(context, workerParameters) {

    companion object {
        const val notificationID = 27
        const val notificationChannelID = "taskChannelID"

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

        fun convertDataToNotification(workerData: Data): Notification {
            return Notification().apply {
                id = workerData.getString(extraNotificationID)!!
                title = workerData.getString(extraNotificationTitle)
                content = workerData.getString(extraNotificationContent)
                data = workerData.getString(extraNotificationData)
                type = workerData.getInt(extraNotificationType, Notification.typeReminder)
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
    }

    protected fun sendNotification(notification: android.app.Notification?) {
        with(NotificationManagerCompat.from(context)) {
            if (getNotificationChannel(notificationChannelID) == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(NotificationChannel(notificationChannelID,
                        context.getString(R.string.notification_channel_reminders),
                        NotificationManager.IMPORTANCE_HIGH))
                }
            }
        }
        manager.notify(notificationID, notification)
    }

    protected fun createNotification(notification: Notification?): android.app.Notification {
        val targetActivity = Intent(context, TaskActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0,
            targetActivity, PendingIntent.FLAG_CANCEL_CURRENT)

        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return NotificationCompat.Builder(context, notificationChannelID).apply {
            setSound(soundUri)
            setSmallIcon(R.drawable.ic_custom_brand)
            setContentIntent(contentIntent)
            setContentTitle(notification?.title)
            setContentText(notification?.content)
        }.build()
    }

    private val manager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}