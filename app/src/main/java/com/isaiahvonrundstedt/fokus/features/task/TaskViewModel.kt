package com.isaiahvonrundstedt.fokus.features.task

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch

class TaskViewModel(private var app: Application): BaseViewModel(app) {

    private var repository = TaskRepository.getInstance(app)
    private var workManager = WorkManager.getInstance(app)
    private var items: LiveData<List<TaskResource>>? = repository.fetch()

    fun fetch(): LiveData<List<TaskResource>>? = items

    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.insert(task, attachmentList)

        // Check if notifications for tasks are turned on and check if
        // the task is not finished, then schedule a notification
        if (PreferenceManager(app).taskReminder && !task.isFinished && task.dueDate!!.isAfterNow) {
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    fun remove(task: Task) = viewModelScope.launch {
        repository.remove(task)

        if (task.isImportant)
            manager?.cancel(task.taskID, BaseWorker.taskNotificationID)

        workManager.cancelUniqueWork(task.taskID)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.update(task, attachmentList)

        // If we have a persistent notification,
        // we should dismiss it when the user updates
        // the task to finish
        if (task.isFinished || !task.isImportant || task.dueDate!!.isBeforeNow)
            manager?.cancel(task.taskID, BaseWorker.taskNotificationID)

        // Check if notifications for tasks is turned on and if the task
        // is not finished then reschedule the notification from
        // WorkManager
        if (PreferenceManager(app).taskReminder && !task.isFinished && task.dueDate!!.isAfterNow) {
            workManager.cancelUniqueWork(task.taskID)
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    private val manager by lazy {
        app.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }
}