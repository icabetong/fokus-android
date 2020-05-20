package com.isaiahvonrundstedt.fokus.features.task

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.core.work.DeadlineScheduler
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch

class TaskViewModel(private var app: Application): BaseViewModel(app) {

    private var dataStore = CoreRepository(app)
    private var workManager = WorkManager.getInstance(app)
    private var items: LiveData<List<Core>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Core>>? = items

    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        dataStore.insert(task, attachmentList)

        if (PreferenceManager(app).remindWhenDue) {
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequestBuilder<DeadlineScheduler>()
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    fun remove(task: Task) = viewModelScope.launch {
        dataStore.remove(task)
        workManager.cancelUniqueWork(task.taskID)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        dataStore.update(task, attachmentList)

        if (PreferenceManager(app).remindWhenDue && !task.isArchived && !task.isFinished) {
            workManager.cancelUniqueWork(task.taskID)
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequestBuilder<DeadlineScheduler>()
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }
}