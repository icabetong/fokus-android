package com.isaiahvonrundstedt.fokus.features.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.extensions.addStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.features.core.extensions.removeStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class TaskAdapter(private var actionListener: ActionListener)
    : BaseAdapter<TaskResource, TaskAdapter.TaskViewHolder>(callback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task,
            parent, false)
        return TaskViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
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

        fun onBind(resource: TaskResource) {
            val id = resource.task.taskID
            taskNameView.transitionName = transitionNameID + id
            dueDateView.transitionName = transitionDateID + id
            subjectNameView.transitionName = transitionSubjectID + id

            with(resource) {
                attachmentView.isVisible = attachmentList.isNotEmpty()
                attachmentView.text =
                    itemView.context.resources.getQuantityString(R.plurals.files_attached,
                        resource.attachmentList.size, resource.attachmentList.size)

                checkBox.isChecked = task.isFinished
                taskNameView.text = task.name
                dueDateView.text = task.formatDueDate(rootView.context)

                if (subject != null) {
                    subjectNameView.text = subject?.code
                    tagView.setImageDrawable(subject?.tintDrawable(tagView.drawable))
                } else
                    subjectNameView.isVisible = false

                if (task.isFinished) taskNameView.addStrikeThroughEffect()
            }

            checkBox.setOnClickListener { view ->
                view as MaterialCheckBox
                resource.task.isFinished = view.isChecked
                if (view.isChecked)
                    taskNameView.addStrikeThroughEffect()
                else taskNameView.removeStrikeThroughEffect()
                actionListener.onActionPerformed(resource, ActionListener.Action.MODIFY,
                    emptyMap())
            }

            rootView.setOnClickListener {
                actionListener.onActionPerformed(resource, ActionListener.Action.SELECT,
                    mapOf(transitionNameID + id to taskNameView, transitionDateID + id to dueDateView))
            }
        }
    }

    companion object {
        const val transitionNameID = "transition:name:"
        const val transitionDateID = "transition:date:"
        const val transitionSubjectID = "transition:schedule:"

        val callback = object : DiffUtil.ItemCallback<TaskResource>() {
            override fun areItemsTheSame(oldItem: TaskResource, newItem: TaskResource): Boolean {
                return oldItem.task.taskID == newItem.task.taskID
            }

            override fun areContentsTheSame(oldItem: TaskResource, newItem: TaskResource): Boolean {
                return oldItem == newItem
            }
        }
    }
}