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
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment

class TaskAdapter(
    private val actionListener: ActionListener,
    private val statusListener: TaskStatusListener,
    private val archiveListener: ArchiveListener
) : BaseAdapter<TaskPackage, TaskAdapter.TaskViewHolder>(TaskPackage.DIFF_CALLBACK), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = LayoutItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return TaskViewHolder(binding.root, actionListener, statusListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        when (direction) {
            ItemTouchHelper.START -> {
                actionListener.onActionPerformed(
                    getItem(position), ActionListener.Action.DELETE,
                    null
                )
            }
            ItemTouchHelper.END -> {
                archiveListener.onItemArchive(getItem(position))
            }
        }
    }

    class TaskViewHolder(
        itemView: View,
        private val actionListener: ActionListener,
        private val statusListener: TaskStatusListener
    ) : BaseViewHolder(itemView) {
        private val binding = LayoutItemTaskBinding.bind(itemView)

        override fun <T> onBind(data: T) {
            if (data is TaskPackage) {
                with(data.task) {
                    binding.root.transitionName = BaseFragment.TRANSITION_ELEMENT_ROOT + taskID

                    val textColorRes = if (isFinished)
                        R.color.color_secondary_text
                    else R.color.color_primary_text

                    binding.checkBox.isChecked = isFinished
                    binding.taskNameView.text = name
                    binding.taskNameView.setTextColorFromResource(textColorRes)
                    binding.taskNameView.setStrikeThroughEffect(isFinished)

                    if (hasDueDate())
                        binding.dueDateView.text = formatDueDate(binding.root.context)
                    else binding.dueDateView.isVisible = false
                }

                if (data.subject != null) {
                    with(binding.subjectView) {
                        text = data.subject?.code
                        setCompoundDrawableAtStart(
                            data.subject?.tintDrawable(
                                getCompoundDrawableAtStart()
                            )
                        )
                    }
                } else binding.subjectView.isVisible = false

                binding.checkBox.setOnClickListener { view ->
                    with(view as AppCompatCheckBox) {
                        data.task.isFinished = isChecked
                        binding.taskNameView.setStrikeThroughEffect(isChecked)
                        if (isChecked)
                            binding.taskNameView.setTextColorFromResource(R.color.color_secondary_text)
                    }
                    statusListener.onStatusChanged(data, view.isChecked)
                }

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(data, ActionListener.Action.SELECT, it)
                }
            }
        }
    }

    interface TaskStatusListener {
        fun onStatusChanged(taskPackage: TaskPackage, isFinished: Boolean)
    }
}