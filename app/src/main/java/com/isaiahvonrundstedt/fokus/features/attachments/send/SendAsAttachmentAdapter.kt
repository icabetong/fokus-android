package com.isaiahvonrundstedt.fokus.features.attachments.send

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemTaskSendBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class SendAsAttachmentAdapter(private val shareListener: ShareListener)
    : BaseAdapter<TaskPackage, SendAsAttachmentAdapter.TaskViewHolder>(TaskPackage.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = LayoutItemTaskSendBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return TaskViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {

        private val binding = LayoutItemTaskSendBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is TaskPackage) {
                binding.titleView.text = t.task.name
                binding.summaryView.text = t.task.formatDueDate(itemView.context)

                binding.addButton.setOnClickListener {
                    shareListener.onShareToTask(t.task.taskID)
                }
            }
        }

    }

    interface ShareListener {
        fun onShareToTask(taskID: String)
    }
}