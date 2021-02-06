package com.isaiahvonrundstedt.fokus.features.archived.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchivedTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
): ViewModel() {

    val items: LiveData<List<TaskPackage>> = taskRepository.fetchArchived()

}