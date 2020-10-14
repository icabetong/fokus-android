package com.isaiahvonrundstedt.fokus.features.attachments.send

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class SendAsAttachmentAdapter(private val shareListener: ShareListener)
    : BaseAdapter<TaskPackage, SendAsAttachmentAdapter.TaskViewHolder>(TaskPackage.DIFF_CALLBACK) {

    var attachmentID: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task_send,
            parent, false)
        return TaskViewHolder(rowView, shareListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View, private val shareListener: ShareListener)
        : BaseAdapter.BaseViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)
        private val addButton: Chip = itemView.findViewById(R.id.addButton)

        override fun <T> onBind(t: T) {
            if (t is TaskPackage) {
                titleView.text = t.task.name
                summaryView.text = t.task.formatDueDate(itemView.context)

                addButton.setOnClickListener {
                    shareListener.onShareToTask(t.task.taskID)
                }
            }
        }

    }

    interface ShareListener {
        fun onShareToTask(taskID: String)
    }
}