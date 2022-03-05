package com.isaiahvonrundstedt.fokus.features.task.archived

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val items: LiveData<List<TaskPackage>> = taskRepository.fetchArchived()
    val isEmpty: LiveData<Boolean> = Transformations.map(items) { it.isEmpty() }

    fun removeFromArchive(taskPackage: TaskPackage) = viewModelScope.launch {
        taskPackage.task.isTaskArchived = false
        taskRepository.update(taskPackage.task)
    }

}