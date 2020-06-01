package com.isaiahvonrundstedt.fokus.features.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.core.extensions.addStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.features.core.extensions.removeStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class TaskAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Core, TaskAdapter.TaskViewHolder>(callback) {

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task,
            parent, false)
        return TaskViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    class TaskViewHolder(itemView: View, private val actionListener: ActionListener)
        : RecyclerView.ViewHolder(itemView) {

        private val rootView: FrameLayout = itemView.findViewById(R.id.rootView)
        private val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkBox)
        private val subjectNameView: TextView = itemView.findViewById(R.id.subjectNameView)
        private val taskNameView: TextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: TextView = itemView.findViewById(R.id.dueDateView)
        private val attachmentView: TextView = itemView.findViewById(R.id.attachmentsView)
        private val tagView: ImageView = itemView.findViewById(R.id.tagView)

        fun onBind(core: Core) {
            with(core) {
                attachmentView.isVisible = attachmentList.isNotEmpty()
                attachmentView.text =
                    itemView.context.resources.getQuantityString(R.plurals.files_attached,
                        core.attachmentList.size, core.attachmentList.size)

                subjectNameView.text = subject.code
                taskNameView.text = task.name
                dueDateView.text = task.formatDueDate(rootView.context)
                tagView.setImageDrawable(subject.tintDrawable(tagView.drawable))
                checkBox.isChecked = task.isFinished

                if (task.isFinished) taskNameView.addStrikeThroughEffect()
            }

            checkBox.setOnClickListener { view ->
                view as MaterialCheckBox
                core.task.isFinished = view.isChecked
                if (view.isChecked)
                    taskNameView.addStrikeThroughEffect()
                else taskNameView.removeStrikeThroughEffect()
                actionListener.onActionPerformed(core, ActionListener.Action.MODIFY)
            }

            rootView.setOnClickListener {
                actionListener.onActionPerformed(core, ActionListener.Action.SELECT)
            }
        }
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<Core>() {
            override fun areItemsTheSame(oldItem: Core, newItem: Core): Boolean {
                return oldItem.task.taskID == newItem.task.taskID
            }

            override fun areContentsTheSame(oldItem: Core, newItem: Core): Boolean {
                return oldItem == newItem
            }
        }
    }
}