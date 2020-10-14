package com.isaiahvonrundstedt.fokus.features.attachments.send

import android.app.Application
import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import kotlinx.coroutines.launch

class SendAsAttachmentViewModel(app: Application): BaseViewModel(app) {

    private val repository = TaskRepository.getInstance(applicationContext)
    private val _tasks: LiveData<List<TaskPackage>> = repository.fetchLiveData()

    val tasks: MediatorLiveData<List<TaskPackage>> = MediatorLiveData()
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
        tasks.addSource(_tasks) {
            tasks.value = it
        }
        attachment.type = Attachment.TYPE_WEBSITE_LINK
    }

    fun addAttachment(taskID: String) = viewModelScope.launch {
        attachment.task = taskID
        repository.addAttachment(attachment)
    }

    fun removeAttachment() = viewModelScope.launch {
        repository.removeAttachment(attachment)
    }
}