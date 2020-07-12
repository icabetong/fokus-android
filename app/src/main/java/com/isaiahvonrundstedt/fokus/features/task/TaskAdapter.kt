package com.isaiahvonrundstedt.fokus.features.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter

class TaskAdapter(private var actionListener: ActionListener)
    : BaseListAdapter<TaskResource, TaskAdapter.TaskViewHolder>(callback) {

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
        : BaseViewHolder(itemView) {

        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val subjectView: TextView = itemView.findViewById(R.id.subjectView)
        private val taskNameView: TextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: TextView = itemView.findViewById(R.id.dueDateView)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is TaskResource) {
                    taskNameView.transitionName = TRANSITION_NAME_ID + task.taskID

                    with(task) {
                        checkBox.isChecked = isFinished
                        taskNameView.text = name
                        dueDateView.text = formatDueDate(rootView.context)

                        taskNameView.setStrikeThroughEffect(isFinished)
                        if (isFinished)
                            taskNameView.setTextColorFromResource(R.color.color_secondary_text)
                    }

                    subjectView.isVisible = subject != null
                    subject?.let {
                        with(subjectView) {
                            text = it.code
                            setCompoundDrawableAtStart(it.tintDrawable(getCompoundDrawableAtStart()))
                        }
                    }

                    checkBox.setOnClickListener { view ->
                        with(view as CheckBox) {
                            task.isFinished = isChecked
                            taskNameView.setStrikeThroughEffect(isChecked)
                            if (isChecked)
                                taskNameView.setTextColorFromResource(R.color.color_secondary_text)
                        }
                        actionListener.onActionPerformed(this, ActionListener.Action.MODIFY,
                            emptyMap())
                    }

                    rootView.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.SELECT,
                            mapOf(TRANSITION_NAME_ID + task.taskID to taskNameView))
                    }
                }
            }
        }
    }

    companion object {
        const val TRANSITION_NAME_ID = "transition:name:"

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