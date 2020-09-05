package com.isaiahvonrundstedt.fokus.features.task

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.widget.task.TaskWidgetProvider
import kotlinx.coroutines.launch

class TaskViewModel(private var app: Application) : BaseViewModel(app) {

    private var repository = TaskRepository.getInstance(app)
    private var pendingItems: LiveData<List<TaskPackage>>? = repository.fetchPending()
    private var finishedItems: LiveData<List<TaskPackage>>? = repository.fetchFinished()

    fun fetchPending(): LiveData<List<TaskPackage>>? = pendingItems

    fun fetchFinished(): LiveData<List<TaskPackage>>? = finishedItems

    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.insert(task, attachmentList)

        // Check if notifications for tasks are turned on and check if
        // the task is not finished, then schedule a notification
        if (preferences.taskReminder && !task.isFinished && task.isDueDateInFuture()) {

            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
                request)
        }

        TaskWidgetProvider.triggerRefresh(app)
    }

    fun remove(task: Task) = viewModelScope.launch {
        repository.remove(task)

        if (task.isImportant)
            notificationService?.cancel(task.taskID, BaseWorker.NOTIFICATION_ID_TASK)

        workManager.cancelUniqueWork(task.taskID)

        TaskWidgetProvider.triggerRefresh(app)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.update(task, attachmentList)

        // If we have a persistent notification,
        // we should dismiss it when the user updates
        // the task to finish
        if (task.isFinished || !task.isImportant || task.dueDate?.isBeforeNow == true)
            notificationService?.cancel(task.taskID, BaseWorker.NOTIFICATION_ID_TASK)

        // Check if notifications for tasks is turned on and if the task
        // is not finished then reschedule the notification from
        // WorkManager
        if (preferences.taskReminder && !task.isFinished && task.isDueDateInFuture()) {
            workManager.cancelUniqueWork(task.taskID)
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
                request)
        }

        TaskWidgetProvider.triggerRefresh(app)
    }
}