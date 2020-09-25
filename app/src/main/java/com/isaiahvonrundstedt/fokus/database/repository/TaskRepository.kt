package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class TaskRepository private constructor(context: Context) {

    private var database = AppDatabase.getInstance(context)
    private var tasks = database.tasks()
    private var attachments = database.attachments()

    companion object {
        private var instance: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            if (instance == null) {
                synchronized(TaskRepository::class) {
                    instance = TaskRepository(context)
                }
            }
            return instance!!
        }
    }

    fun fetchLiveData(): LiveData<List<TaskPackage>> = tasks.fetchLiveData()

    suspend fun fetchCore(): List<Task> = tasks.fetchCore()

    suspend fun fetchCount(): Int = tasks.fetchCount()

    suspend fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) {
        tasks.insert(task)
        if (attachmentList.isNotEmpty())
            attachmentList.forEach { attachments.insert(it) }
    }

    suspend fun remove(task: Task) {
        tasks.remove(task)
    }

    suspend fun update(task: Task, attachmentList: List<Attachment> = emptyList()) {
        tasks.update(task)
        attachments.removeUsingTaskID(task.taskID)
        if (attachmentList.isNotEmpty())
            attachmentList.forEach { attachments.insert(it) }
    }

    suspend fun setFinished(taskID: String, status: Boolean) {
        if (status)
            tasks.setFinished(taskID, 1)
        else tasks.setFinished(taskID, 0)
    }
}