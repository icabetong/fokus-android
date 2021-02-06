package com.isaiahvonrundstedt.fokus.features.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemSubjectBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemSubjectSingleBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor

class SubjectAdapter(private val actionListener: ActionListener,
                     private val scheduleListener: ScheduleListener,
                     private val archiveListener: ArchiveListener)
    : BaseAdapter<SubjectPackage, BaseAdapter.BaseViewHolder>(SubjectPackage.DIFF_CALLBACK), Swipeable {

    var constraint = SubjectViewModel.Constraint.TODAY

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when(viewType) {
            ITEM_TYPE_ALL_SCHEDULE -> {
                val binding = LayoutItemSubjectBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
                CoreViewHolder(binding.root)
            }
            ITEM_TYPE_SINGLE_SCHEDULE_TODAY -> {
                val binding = LayoutItemSubjectSingleBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
                TodayViewHolder(binding.root)
            }
            ITEM_TYPE_SINGLE_SCHEDULE_TOMORROW -> {
                val binding = LayoutItemSubjectSingleBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
                TomorrowViewHolder(binding.root)
            }
            else -> throw IllegalStateException("Unknown Item type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (constraint) {
            SubjectViewModel.Constraint.ALL -> ITEM_TYPE_ALL_SCHEDULE
            SubjectViewModel.Constraint.TODAY -> ITEM_TYPE_SINGLE_SCHEDULE_TODAY
            SubjectViewModel.Constraint.TOMORROW -> ITEM_TYPE_SINGLE_SCHEDULE_TOMORROW
        }
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                null)
    }

    inner class CoreViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val binding = LayoutItemSubjectBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    binding.root.transitionName = BaseEditor.TRANSITION_ELEMENT_ROOT +
                            t.subject.subjectID

                    binding.tagView.setImageDrawable(tintDrawable(binding.tagView.drawable))
                    binding.nameView.text = code
                    binding.descriptionView.text = description
                }

                binding.scheduleView.setOnClickListener {
                    scheduleListener.onScheduleListener(t.schedules)
                }

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT, it)
                }
            }
        }
    }

    inner class TodayViewHolder(itemView: View): BaseViewHolder(itemView) {

        private val binding = LayoutItemSubjectSingleBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    binding.root.transitionName = BaseEditor.TRANSITION_ELEMENT_ROOT + t.subject.subjectID

                    binding.tagView.setImageDrawable(tintDrawable(binding.tagView.drawable))
                    binding.nameView.text = code
                    binding.descriptionView.text = description
                }

                val todaySchedule = t.getScheduleToday()
                binding.scheduleView.text = todaySchedule?.formatBothTime(binding.root.context)

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT, it)
                }
            }
        }
    }

    inner class TomorrowViewHolder(itemView: View): BaseViewHolder(itemView) {

        private val binding = LayoutItemSubjectSingleBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    binding.root.transitionName = BaseEditor.TRANSITION_ELEMENT_ROOT + t.subject.subjectID

                    binding.tagView.setImageDrawable(tintDrawable(binding.tagView.drawable))
                    binding.nameView.text = code
                    binding.descriptionView.text = description
                }

                val tomorrowSchedule = t.getScheduleTomorrow()
                binding.scheduleView.text = tomorrowSchedule?.format(itemView.context)

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT, it)
                }
            }
        }
    }

    interface ScheduleListener {
        fun onScheduleListener(items: List<Schedule>)
    }

    companion object {
        const val ITEM_TYPE_ALL_SCHEDULE = 0
        const val ITEM_TYPE_SINGLE_SCHEDULE_TODAY = 1
        const val ITEM_TYPE_SINGLE_SCHEDULE_TOMORROW = 2
    }
}