package com.isaiahvonrundstedt.fokus.features.task

import androidx.room.Embedded
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.subject.Subject

/**
 *  Data class used for data representation
 *  of tasks, subjects and its attachments
 *  in the UI
 */
data class TaskResource @JvmOverloads constructor (
    @Embedded
    var task: Task,
    @Embedded
    var subject: Subject? = null,
    @Relation(entity = Attachment::class, parentColumn = "taskID", entityColumn = "task")
    var attachments: List<Attachment> = emptyList()
)