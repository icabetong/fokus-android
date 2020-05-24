package com.isaiahvonrundstedt.fokus.features.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import org.joda.time.DateTime

class TaskAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Core, TaskAdapter.TaskViewHolder>(callback) {

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task,
            parent, false)
        return TaskViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    inner class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkBox)
        private val subjectNameView: AppCompatTextView = itemView.findViewById(R.id.subjectNameView)
        private val taskNameView: AppCompatTextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: AppCompatTextView = itemView.findViewById(R.id.dueDateView)
        private val attachmentView: AppCompatTextView = itemView.findViewById(R.id.attachmentsView)
        private val tagView: View = itemView.findViewById(R.id.tagView)

        private fun formatDate(core: Core) {
            val currentTime = DateTime.now()
            if (core.task.dueDate!!.isBefore(currentTime) && !core.task.isFinished) {
                dueDateView.setTextColor(ContextCompat.getColor(rootView.context,
                    R.color.colorSwipeLeft))
                dueDateView.text = String.format(rootView.context.getString(R.string.missed),
                    core.task.formatDueDate(rootView.context))
            } else {
                dueDateView.text = core.task.formatDueDate(rootView.context)
                dueDateView.setTextColor(ContextCompat.getColor(rootView.context,
                    R.color.colorOnSurface))
            }
        }

        fun onBind(core: Core) {
            checkBox.setOnClickListener { view ->
                view as MaterialCheckBox
                core.task.isFinished = view.isChecked
                if (view.isChecked) {
                    taskNameView.paintFlags = taskNameView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    formatDate(core)
                } else
                    taskNameView.paintFlags = taskNameView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                actionListener.onActionPerformed(core, ActionListener.Action.MODIFY)
            }

            if (core.task.isFinished)
                taskNameView.paintFlags = taskNameView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            attachmentView.isVisible = core.attachmentList.isNotEmpty()
            attachmentView.text = itemView.context.resources.getQuantityString(R.plurals.files_attached,
                core.attachmentList.size, core.attachmentList.size)

            rootView.setOnClickListener {
                actionListener.onActionPerformed(core, ActionListener.Action.SELECT)
            }

            subjectNameView.text = core.subject.description ?: core.subject.code
            tagView.setBackgroundColor(core.subject.tag.actualColor)

            checkBox.isChecked = core.task.isFinished
            taskNameView.text = core.task.name
            formatDate(core)
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<Core>() {
            override fun areItemsTheSame(oldItem: Core, newItem: Core): Boolean {
                return oldItem.task.taskID == newItem.task.taskID
            }

            override fun areContentsTheSame(oldItem: Core, newItem: Core): Boolean {
                return oldItem == newItem
            }
        }
    }
}