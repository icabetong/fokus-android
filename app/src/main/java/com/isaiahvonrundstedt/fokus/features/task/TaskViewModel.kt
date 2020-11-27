package com.isaiahvonrundstedt.fokus.features.task

import android.app.NotificationManager
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isBeforeNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.notifications.task.TaskNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManager,
) : ViewModel() {

    private val _tasks: LiveData<List<TaskPackage>> = repository.fetchLiveData()

    val tasks: MediatorLiveData<List<TaskPackage>> = MediatorLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(tasks) { it.isNullOrEmpty() }

    var filterOption = preferenceManager.taskConstraint
        set(value) {
            field = value
            preferenceManager.taskConstraint = value
            rearrange(value, sort, sortDirection)
        }

    var sort: Sort = preferenceManager.tasksSort
        set(value) {
            field = value
            rearrange(filterOption, value, sortDirection)
        }

    var sortDirection: SortDirection = preferenceManager.tasksSortDirection
        set(value) {
            field = value
            rearrange(filterOption, sort, value)
        }

    init {
        tasks.addSource(_tasks) { items ->
            when (filterOption) {
                Constraint.ALL ->
                    tasks.value = items
                Constraint.PENDING ->
                    tasks.value = items.filter { !it.task.isFinished }
                Constraint.FINISHED ->
                    tasks.value = items.filter { it.task.isFinished }
            }
        }
    }


    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.insert(task, attachmentList)

        // Check if notifications for tasks are turned on and check if
        // the task is not finished, then schedule a notification
        if (preferenceManager.taskReminder && !task.isFinished && task.isDueDateInFuture()) {

            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .addTag(task.taskID)
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
                request)
        }
    }

    fun remove(task: Task) = viewModelScope.launch {
        repository.remove(task)

        if (task.isImportant)
            notificationManager.cancel(task.taskID, BaseWorker.NOTIFICATION_ID_TASK)

        workManager.cancelUniqueWork(task.taskID)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.update(task, attachmentList)

        // If we have a persistent notification,
        // we should dismiss it when the user updates
        // the task to finish
        if (task.isFinished || !task.isImportant || task.dueDate?.isBeforeNow() == true)
            notificationManager.cancel(task.taskID, BaseWorker.NOTIFICATION_ID_TASK)

        // Check if notifications for tasks is turned on and if the task
        // is not finished then reschedule the notification from
        // WorkManager
        if (preferenceManager.taskReminder && !task.isFinished && task.isDueDateInFuture()) {
            workManager.cancelUniqueWork(task.taskID)
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .addTag(task.taskID)
                .build()
            workManager.enqueueUniqueWork(task.taskID, ExistingWorkPolicy.REPLACE,
                request)
        }
    }

    private fun rearrange(filter: Constraint, sort: Sort, direction: SortDirection)
        = when(filter) {
        Constraint.ALL -> {
            _tasks.value?.let { items ->
                tasks.value = when (sort) {
                    Sort.NAME -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.sortedBy { it.task.name }
                            SortDirection.DESCENDING ->
                                items.sortedByDescending { it.task.name }
                        }
                    }
                    Sort.DUE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.sortedBy { it.task.dueDate }
                            SortDirection.DESCENDING ->
                                items.sortedByDescending { it.task.dueDate }
                        }
                    }
                }
            }
        }
        Constraint.PENDING -> {
            _tasks.value?.let { items ->
                tasks.value = when (sort) {
                    Sort.NAME -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { !it.task.isFinished }
                                    .sortedBy { it.task.name }
                            SortDirection.DESCENDING ->
                                items.filter { !it.task.isFinished }
                                    .sortedByDescending { it.task.name }
                        }
                    }
                    Sort.DUE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { !it.task.isFinished }
                                    .sortedBy { it.task.dueDate }
                            SortDirection.DESCENDING ->
                                items.filter { !it.task.isFinished }
                                    .sortedByDescending { it.task.dueDate }
                        }
                    }
                }
            }
        }
        Constraint.FINISHED -> {
            _tasks.value?.let { items ->
                tasks.value = when (sort) {
                    Sort.NAME -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.task.isFinished }
                                    .sortedBy { it.task.name }
                            SortDirection.DESCENDING ->
                                items.filter { it.task.isFinished }
                                    .sortedByDescending { it.task.name }
                        }
                    }
                    Sort.DUE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.task.isFinished }
                                    .sortedBy { it.task.dueDate }
                            SortDirection.DESCENDING ->
                                items.filter { it.task.isFinished }
                                    .sortedByDescending { it.task.dueDate }
                        }
                    }
                }
            }
        }
    }

    enum class Sort {
        NAME, DUE;

        companion object {
            fun parse(value: String): Sort {
                return when(value) {
                    NAME.toString() -> NAME
                    DUE.toString() -> DUE
                    else -> NAME
                }
            }
        }
    }

    enum class Constraint {
        ALL, PENDING, FINISHED;

        companion object {
            fun parse(value: String): Constraint {
                return when(value) {
                    ALL.toString() -> ALL
                    PENDING.toString() -> PENDING
                    FINISHED.toString() -> FINISHED
                    else -> PENDING
                }
            }
        }
    }
}