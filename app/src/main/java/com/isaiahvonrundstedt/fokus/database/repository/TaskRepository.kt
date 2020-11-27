package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.dao.AttachmentDAO
import com.isaiahvonrundstedt.fokus.database.dao.TaskDAO
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import com.isaiahvonrundstedt.fokus.features.widget.task.TaskWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TaskRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val tasks: TaskDAO,
    private val attachments: AttachmentDAO,
) {

    fun fetchLiveData(): LiveData<List<TaskPackage>> = tasks.fetchLiveData()

    suspend fun fetchCore(): List<Task> = tasks.fetchCore()

    suspend fun fetchCount(): Int = tasks.fetchCount()

    suspend fun insert(task: Task, attachmentList: List<Attachment> = emptyList()) {
        tasks.insert(task)
        if (attachmentList.isNotEmpty())
            attachmentList.forEach { attachments.insert(it) }

        TaskWidgetProvider.triggerRefresh(context)
    }

    suspend fun remove(task: Task) {
        tasks.remove(task)

        TaskWidgetProvider.triggerRefresh(context)
    }

    suspend fun update(task: Task, attachmentList: List<Attachment> = emptyList()) {
        tasks.update(task)
        attachments.removeUsingTaskID(task.taskID)
        if (attachmentList.isNotEmpty())
            attachmentList.forEach { attachments.insert(it) }

        TaskWidgetProvider.triggerRefresh(context)
    }

    suspend fun setFinished(taskID: String, status: Boolean) {
        if (status)
            tasks.setFinished(taskID, 1)
        else tasks.setFinished(taskID, 0)
    }

    suspend fun addAttachment(attachment: Attachment) {
        attachments.insert(attachment)
    }

    suspend fun removeAttachment(attachment: Attachment) {
        attachments.remove(attachment)
    }

}