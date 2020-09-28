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
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.editor.TaskEditor

class TaskAdapter(private var actionListener: ActionListener,
                  private var taskCompletionListener: TaskCompletionListener)
    : BaseAdapter<TaskPackage, TaskAdapter.TaskViewHolder>(callback), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task,
            parent, false)
        return TaskViewHolder(rowView, actionListener, taskCompletionListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
    }

    class TaskViewHolder(itemView: View,
                         private val actionListener: ActionListener,
                         private val taskCompletionListener: TaskCompletionListener) : BaseViewHolder(itemView) {

        private val checkBox: AppCompatCheckBox = itemView.findViewById(R.id.checkBox)
        private val subjectView: TextView = itemView.findViewById(R.id.subjectView)
        private val taskNameView: TextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: TextView = itemView.findViewById(R.id.dueDateView)

        override fun <T> onBind(t: T) {
            if (t is TaskPackage) {
                with(t.task) {
                    taskNameView.transitionName = TaskEditor.TRANSITION_ID_NAME + taskID

                    val textColorRes = if (isFinished)
                        R.color.color_secondary_text
                    else R.color.color_primary_text

                    checkBox.isChecked = isFinished
                    taskNameView.text = name
                    taskNameView.setTextColorFromResource(textColorRes)
                    taskNameView.setStrikeThroughEffect(isFinished)
                    dueDateView.text = formatDueDate(rootView.context)
                }

                if (t.subject != null) {
                    with(subjectView) {
                        text = t.subject?.code
                        setCompoundDrawableAtStart(t.subject?.tintDrawable(getCompoundDrawableAtStart()))
                    }
                } else subjectView.isVisible = false

                checkBox.setOnClickListener { view ->
                    with(view as AppCompatCheckBox) {
                        t.task.isFinished = isChecked
                        taskNameView.setStrikeThroughEffect(isChecked)
                        if (isChecked)
                            taskNameView.setTextColorFromResource(R.color.color_secondary_text)
                    }
                    taskCompletionListener.onTaskCompleted(t, view.isChecked)
                }

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(this, ActionListener.Action.SELECT,
                        mapOf(TaskEditor.TRANSITION_ID_NAME + t.task.taskID to taskNameView))
                }
            }
        }
    }

    interface TaskCompletionListener {
        fun onTaskCompleted(taskPackage: TaskPackage, isChecked: Boolean)
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