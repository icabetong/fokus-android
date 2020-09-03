package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class TaskRepository private constructor(app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var tasks = database?.tasks()
    private var attachments = database?.attachments()

    companion object {
        private var instance: TaskRepository? = null

        fun getInstance(app: Application): TaskRepository {
            if (instance == null) {
                synchronized(TaskRepository::class) {
                    instance = TaskRepository(app)
                }
            }
            return instance!!
        }
    }

    fun fetch(): LiveData<List<TaskPackage>>? = tasks?.fetchLiveData(0)

    fun fetchCompleted(): LiveData<List<TaskPackage>>? = tasks?.fetchLiveData(1)

    suspend fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) {
        tasks?.insert(task)
        if (attachmentList.isNotEmpty())
            attachmentList.forEach { attachments?.insert(it) }
    }

    suspend fun remove(task: Task) {
        tasks?.remove(task)
    }

    suspend fun update(task: Task, attachmentList: List<Attachment> = emptyList()) {
        tasks?.update(task)
        attachments?.removeUsingTaskID(task.taskID)
        if (attachmentList.isNotEmpty())
            attachmentList.forEach { attachments?.insert(it) }
    }
}