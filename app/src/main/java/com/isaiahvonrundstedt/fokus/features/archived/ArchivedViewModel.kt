package com.isaiahvonrundstedt.fokus.features.archived

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.task.Task
import kotlinx.coroutines.launch

class ArchivedViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = CoreRepository(app)
    private var items: LiveData<List<Core>>? = dataStore.fetchArchived()

    fun fetch(): LiveData<List<Core>>? = items

    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        dataStore.insert(task, attachmentList)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        dataStore.insert(task, attachmentList)
    }

    fun remove(task: Task) = viewModelScope.launch {
        dataStore.insert(task)
    }

    fun clear() = viewModelScope.launch {
        dataStore.clearArchived()
    }

}