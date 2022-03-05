package com.isaiahvonrundstedt.fokus.features.task.archived

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemArchivedTaskBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

class ArchivedTaskAdapter(private val listener: SelectListener) :
    BaseAdapter<TaskPackage, ArchivedTaskAdapter.ArchivedTaskViewHolder>(TaskPackage.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedTaskViewHolder {
        val binding = LayoutItemArchivedTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ArchivedTaskViewHolder(binding.root, listener)
    }

    override fun onBindViewHolder(holder: ArchivedTaskViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ArchivedTaskViewHolder(itemView: View, private val listener: SelectListener) :
        BaseViewHolder(itemView) {
        private val binding = LayoutItemArchivedTaskBinding.bind(itemView)

        override fun <T> onBind(data: T) {
            if (data is TaskPackage) {
                with(data.task) {
                    binding.root.transitionName = BaseFragment.TRANSITION_ELEMENT_ROOT + taskID

                    val textColorRes = if (isFinished)
                        R.color.color_secondary_text
                    else R.color.color_primary_text

                    binding.taskNameView.text = name
                    binding.taskNameView.setTextColorFromResource(textColorRes)
                    binding.taskNameView.setStrikeThroughEffect(isFinished)

                    if (hasDueDate())
                        binding.dueDateView.text = formatDueDate(binding.root.context)
                    else binding.dueDateView.isVisible = false
                }

                binding.root.setOnClickListener {
                    listener.onItemSelected(data)
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
            }
        }
    }
}