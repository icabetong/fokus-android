package com.isaiahvonrundstedt.fokus.features.attachments.attach

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttachToTaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks: LiveData<List<TaskPackage>> = repository.fetchLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(tasks) { it.isNullOrEmpty() }

    var attachment = Attachment()
    var subject: String? = null
        set(value) {
            field = value
            attachment.name = value
        }
    var url: String? = null
        set(value) {
            field = value
            attachment.target = value
        }

    init {
        attachment.type = Attachment.TYPE_WEBSITE_LINK
    }

    fun addAttachment(taskID: String) = viewModelScope.launch {
        attachment.task = taskID
        repository.addAttachment(attachment)
    }

}