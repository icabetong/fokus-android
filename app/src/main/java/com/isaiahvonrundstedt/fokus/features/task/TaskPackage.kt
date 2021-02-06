package com.isaiahvonrundstedt.fokus.features.task

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.tag.Tag
import com.isaiahvonrundstedt.fokus.features.tag.TagTaskCrossRef
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
    var attachments: List<Attachment> = emptyList(),
    @Relation(parentColumn = "taskID", entityColumn = "tagID", associateBy = Junction(TagTaskCrossRef::class))
    var tags: List<Tag> = emptyList()
): Parcelable {

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<TaskPackage>() {
            override fun areItemsTheSame(oldItem: TaskPackage, newItem: TaskPackage): Boolean {
                return oldItem.task.taskID == newItem.task.taskID
            }

            override fun areContentsTheSame(oldItem: TaskPackage, newItem: TaskPackage): Boolean {
                return oldItem == newItem
            }
        }
    }
}