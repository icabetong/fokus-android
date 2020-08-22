package com.isaiahvonrundstedt.fokus.features.task

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize

/**
 *  Data class used for data representation
 *  of tasks, subjects and its attachments
 *  in the UI
 */
@Parcelize
data class TaskPackage @JvmOverloads constructor(
    @Embedded
    var task: Task,
    @Embedded
    var subject: Subject? = null,
    @Relation(entity = Attachment::class, parentColumn = "taskID", entityColumn = "task")
    var attachments: List<Attachment> = emptyList()
): Parcelable