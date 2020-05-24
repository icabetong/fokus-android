package com.isaiahvonrundstedt.fokus.features.task

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch

class TaskViewModel(private var app: Application): BaseViewModel(app) {

    private var repository = CoreRepository.getInstance(app)
    private var workManager = WorkManager.getInstance(app)
    private var items: LiveData<List<Core>>? = repository.fetch()

    fun fetch(): LiveData<List<Core>>? = items

    fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.insert(task, attachmentList)

        if (PreferenceManager(app).taskReminder && !task.isFinished) {
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    fun remove(task: Task) = viewModelScope.launch {
        repository.remove(task)
        workManager.cancelUniqueWork(task.taskID)
    }

    fun update(task: Task, attachmentList: List<Attachment> = emptyList()) = viewModelScope.launch {
        repository.update(task, attachmentList)

        if (PreferenceManager(app).taskReminder && !task.isFinished) {
            workManager.cancelUniqueWork(task.taskID)
            val data = BaseWorker.convertTaskToData(task)
            val request = OneTimeWorkRequest.Builder(TaskNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }
}