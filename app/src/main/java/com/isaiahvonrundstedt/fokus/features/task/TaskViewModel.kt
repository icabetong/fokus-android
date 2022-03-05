package com.isaiahvonrundstedt.fokus.features.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val _tasks: LiveData<List<TaskPackage>> = repository.fetchLiveData()

    var tasksCompose by mutableStateOf(listOf<TaskPackage>())
        private set

    init {
        viewModelScope.launch {
            tasksCompose = repository.fetchAsPackage()
        }
    }

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

    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) =
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            repository.insert(task, attachmentList)
        }

    fun remove(task: Task) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.remove(task)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) =
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            repository.update(task, attachmentList)
        }

    private fun rearrange(filter: Constraint, sort: Sort, direction: SortDirection) =
        when (filter) {
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
                return when (value) {
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
                return when (value) {
                    ALL.toString() -> ALL
                    PENDING.toString() -> PENDING
                    FINISHED.toString() -> FINISHED
                    else -> PENDING
                }
            }
        }
    }
}