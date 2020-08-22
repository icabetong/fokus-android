package com.isaiahvonrundstedt.fokus.features.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter

class TaskAdapter(private var actionListener: ActionListener,
                  private var taskCompletionListener: TaskCompletionListener)
    : BaseListAdapter<TaskPackage, TaskAdapter.TaskViewHolder>(callback), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task,
            parent, false)
        return TaskViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
    }

    inner class TaskViewHolder(itemView: View) : BaseViewHolder(itemView) {

        private val checkBox: AppCompatCheckBox = itemView.findViewById(R.id.checkBox)
        private val subjectView: TextView = itemView.findViewById(R.id.subjectView)
        private val taskNameView: TextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: TextView = itemView.findViewById(R.id.dueDateView)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is TaskPackage) {
                    taskNameView.transitionName = TaskEditor.TRANSITION_ID_NAME + task.taskID

                    with(task) {
                        checkBox.isChecked = isFinished
                        taskNameView.text = name
                        taskNameView.setStrikeThroughEffect(isFinished)

                        if (hasDueDate())
                            dueDateView.text = formatDueDate(rootView.context)
                        else dueDateView.isVisible = false

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
                        with(view as AppCompatCheckBox) {
                            task.isFinished = isChecked
                            taskNameView.setStrikeThroughEffect(isChecked)
                            if (isChecked)
                                taskNameView.setTextColorFromResource(R.color.color_secondary_text)
                        }
                        taskCompletionListener.onTaskCompleted(this, view.isChecked)
                    }

                    rootView.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.SELECT,
                            mapOf(TaskEditor.TRANSITION_ID_NAME + task.taskID to taskNameView))
                    }
                }
            }
        }
    }

    interface TaskCompletionListener {
        fun <T> onTaskCompleted(t: T, isChecked: Boolean)
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<TaskPackage>() {
            override fun areItemsTheSame(oldItem: TaskPackage, newItem: TaskPackage): Boolean {
                return oldItem.task.taskID == newItem.task.taskID
            }

            override fun areContentsTheSame(oldItem: TaskPackage, newItem: TaskPackage): Boolean {
                return oldItem == newItem
            }
        }
    }
}