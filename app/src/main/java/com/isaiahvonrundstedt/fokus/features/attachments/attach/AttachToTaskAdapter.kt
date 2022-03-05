package com.isaiahvonrundstedt.fokus.features.attachments.attach

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemTaskSendBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class AttachToTaskAdapter(private val selectListener: SelectListener) :
    BaseAdapter<TaskPackage, AttachToTaskAdapter.TaskViewHolder>(TaskPackage.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = LayoutItemTaskSendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return TaskViewHolder(binding.root, selectListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class TaskViewHolder(
        itemView: View,
        private val selectListener: SelectListener
    ) : BaseAdapter.BaseViewHolder(itemView) {
        private val binding = LayoutItemTaskSendBinding.bind(itemView)

        override fun <T> onBind(data: T) {
            if (data is TaskPackage) {
                binding.titleView.text = data.task.name
                binding.summaryView.text = data.task.formatDueDate(itemView.context)

                binding.addButton.setOnClickListener {
                    selectListener.onItemSelected(data)
                }
            }
        }
    }
}