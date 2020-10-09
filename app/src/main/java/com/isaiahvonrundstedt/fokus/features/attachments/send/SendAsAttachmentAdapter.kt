package com.isaiahvonrundstedt.fokus.features.attachments.send

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class SendAsAttachmentAdapter(private val actionListener: ActionListener)
    : BaseAdapter<TaskPackage, SendAsAttachmentAdapter.TaskViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task_send,
            parent, false)
        return TaskViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class TaskViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseAdapter.BaseViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)
        private val addButton: Chip = itemView.findViewById(R.id.addButton)

        override fun <T> onBind(t: T) {
            if (t is TaskPackage) {
                var isAdded = false

                titleView.text = t.task.name
                summaryView.text = t.task.formatDueDate(itemView.context)

                addButton.setOnClickListener {
                    val action = if (isAdded) ActionListener.Action.REMOVE
                        else ActionListener.Action.ADD
                    isAdded = true

                    val resID: Int = if (isAdded) R.string.button_remove
                        else R.string.button_add
                    addButton.setText(resID)

                    actionListener.onAction(action, t.task.taskID)
                }
            }
        }
    }

    interface ActionListener {
        fun onAction(action: Action, taskID: String)

        enum class Action {
            ADD, REMOVE
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<TaskPackage>() {
            override fun areItemsTheSame(oldItem: TaskPackage, newItem: TaskPackage): Boolean {
                return oldItem.task.taskID == oldItem.task.taskID
            }
            override fun areContentsTheSame(oldItem: TaskPackage, newItem: TaskPackage): Boolean {
                return oldItem == newItem
            }
        }
    }
}