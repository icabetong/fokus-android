package com.isaiahvonrundstedt.fokus.features.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemTaskBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor

class TaskAdapter(private var actionListener: ActionListener,
                  private var taskCompletionListener: TaskCompletionListener)
    : BaseAdapter<TaskPackage, TaskAdapter.ViewHolder>(TaskPackage.DIFF_CALLBACK), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemTaskBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                null)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        private val binding = LayoutItemTaskBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is TaskPackage) {
                with(t.task) {
                    binding.root.transitionName = BaseEditor.TRANSITION_ELEMENT_ROOT + taskID

                    val textColorRes = if (isFinished)
                        R.color.color_secondary_text
                    else R.color.color_primary_text

                    binding.checkBox.isChecked = isFinished
                    binding.taskNameView.text = name
                    binding.taskNameView.setTextColorFromResource(textColorRes)
                    binding.taskNameView.setStrikeThroughEffect(isFinished)
                    binding.dueDateView.text = formatDueDate(binding.root.context)
                }

                if (t.subject != null) {
                    with(binding.subjectView) {
                        text = t.subject?.code
                        setCompoundDrawableAtStart(t.subject?.tintDrawable(getCompoundDrawableAtStart()))
                    }
                } else binding.subjectView.isVisible = false

                binding.checkBox.setOnClickListener { view ->
                    with(view as AppCompatCheckBox) {
                        t.task.isFinished = isChecked
                        binding.taskNameView.setStrikeThroughEffect(isChecked)
                        if (isChecked)
                            binding.taskNameView.setTextColorFromResource(R.color.color_secondary_text)
                    }
                    taskCompletionListener.onTaskCompleted(t, view.isChecked)
                }

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT, it)
                }
            }
        }
    }

    interface TaskCompletionListener {
        fun onTaskCompleted(taskPackage: TaskPackage, isChecked: Boolean)
    }

}