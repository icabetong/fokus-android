package com.isaiahvonrundstedt.fokus.features.task

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isBeforeNow
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.notifications.task.TaskNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.widget.task.TaskWidgetProvider
import kotlinx.coroutines.launch

class TaskViewModel(private var app: Application) : BaseViewModel(app) {

    private val repository = TaskRepository.getInstance(app)
    private val _tasks: LiveData<List<TaskPackage>> = repository.fetchLiveData()

    val tasks: MediatorLiveData<List<TaskPackage>> = MediatorLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(tasks) { it.isNullOrEmpty() }

    var filterOption = FilterOption.PENDING
        set(value) {
            field = value
            performFilter(value)
        }

    init {
        tasks.addSource(_tasks) { items ->
            when (filterOption) {
                FilterOption.ALL ->
                    tasks.value = items
                FilterOption.PENDING ->
                    tasks.value = items.filter { !it.task.isFinished }
                FilterOption.FINISHED ->
                    tasks.value = items.filter { it.task.isFinished }
            }
        }
    }


    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.insert(task, attachmentList)

        // Check if notifications for tasks are turned on and check if
        // the task is not finished, then schedule a notification
        if (preferences.taskReminder && !task.isFinished && task.isDueDateInFuture()) {

            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .addTag(task.taskID)
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
        if (task.isFinished || !task.isImportant || task.dueDate?.isBeforeNow() == true)
            notificationService?.cancel(task.taskID, BaseWorker.NOTIFICATION_ID_TASK)

        // Check if notifications for tasks is turned on and if the task
        // is not finished then reschedule the notification from
        // WorkManager
        if (preferences.taskReminder && !task.isFinished && task.isDueDateInFuture()) {
            workManager.cancelUniqueWork(task.taskID)
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .addTag(task.taskID)
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
                request)
        }

        TaskWidgetProvider.triggerRefresh(app)
    }

    private fun performFilter(option: FilterOption) = when(option) {
        FilterOption.ALL ->
            _tasks.value?.let { tasks.value = it }
        FilterOption.PENDING ->
            _tasks.value?.let { tasks.value = it.filter { task -> !task.task.isFinished } }
        FilterOption.FINISHED ->
            _tasks.value?.let { tasks.value = it.filter { task -> task.task.isFinished} }
    }

    enum class FilterOption {
        ALL, PENDING, FINISHED
    }
}