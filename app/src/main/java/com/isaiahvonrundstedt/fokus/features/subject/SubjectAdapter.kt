package com.isaiahvonrundstedt.fokus.features.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor

class SubjectAdapter(private var actionListener: ActionListener,
                     private val scheduleListener: ScheduleListener)
    : BaseAdapter<SubjectPackage, BaseAdapter.BaseViewHolder>(callback), Swipeable {

    var currentOption = SubjectViewModel.FilterOption.TODAY

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val resID: Int = if (viewType == ITEM_TYPE_ALL_SCHEDULE) R.layout.layout_item_subject
            else R.layout.layout_item_subject_single

        val rowView: View = LayoutInflater.from(parent.context).inflate(resID, parent, false)
        return when(viewType) {
            ITEM_TYPE_ALL_SCHEDULE -> CoreViewHolder(rowView, actionListener, scheduleListener)
            ITEM_TYPE_SINGLE_SCHEDULE_TODAY -> TodayViewHolder(rowView, actionListener)
            ITEM_TYPE_SINGLE_SCHEDULE_TOMORROW -> TomorrowViewHolder(rowView, actionListener)
            else -> throw IllegalStateException("Unknown Item type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentOption) {
            SubjectViewModel.FilterOption.ALL -> ITEM_TYPE_ALL_SCHEDULE
            SubjectViewModel.FilterOption.TODAY -> ITEM_TYPE_SINGLE_SCHEDULE_TODAY
            SubjectViewModel.FilterOption.TOMORROW -> ITEM_TYPE_SINGLE_SCHEDULE_TOMORROW
        }
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
    }

    private class CoreViewHolder(itemView: View, private val actionListener: ActionListener,
        private val scheduleListener: ScheduleListener)
        : BaseViewHolder(itemView) {

        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)
        private val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        private val scheduleView: Chip = itemView.findViewById(R.id.scheduleView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    nameView.transitionName = SubjectEditor.TRANSITION_ID_CODE + subjectID
                    descriptionView.transitionName = SubjectEditor.TRANSITION_ID_DESCRIPTION + subjectID

                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    nameView.text = code
                    descriptionView.text = description
                }

                scheduleView.setOnClickListener {
                    scheduleListener.onScheduleListener(t.schedules)
                }

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT,
                        mapOf(SubjectEditor.TRANSITION_ID_CODE + t.subject.subjectID to nameView,
                            SubjectEditor.TRANSITION_ID_DESCRIPTION + t.subject.subjectID to descriptionView))
                }
            }
        }
    }

    private class TodayViewHolder(itemView: View, actionListener: ActionListener)
        : SingleScheduleViewHolder(itemView, actionListener) {

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    nameView.transitionName = SubjectEditor.TRANSITION_ID_CODE + subjectID
                    descriptionView.transitionName = SubjectEditor.TRANSITION_ID_DESCRIPTION + subjectID

                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    nameView.text = code
                    descriptionView.text = description
                }

                val todaySchedule = t.getScheduleToday()
                scheduleView.text = todaySchedule?.formatBothTime()

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT,
                        mapOf(SubjectEditor.TRANSITION_ID_CODE + t.subject.subjectID to nameView,
                            SubjectEditor.TRANSITION_ID_DESCRIPTION + t.subject.subjectID to descriptionView))
                }
            }
        }
    }

    private class TomorrowViewHolder(itemView: View, actionListener: ActionListener)
        : SingleScheduleViewHolder(itemView, actionListener) {

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    nameView.transitionName = SubjectEditor.TRANSITION_ID_CODE + subjectID
                    descriptionView.transitionName = SubjectEditor.TRANSITION_ID_DESCRIPTION + subjectID

                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    nameView.text = code
                    descriptionView.text = description
                }

                val tomorrowSchedule = t.getScheduleTomorrow()
                scheduleView.text = tomorrowSchedule?.format(itemView.context)

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT,
                        mapOf(SubjectEditor.TRANSITION_ID_CODE + t.subject.subjectID to nameView,
                            SubjectEditor.TRANSITION_ID_DESCRIPTION + t.subject.subjectID to descriptionView))
                }
            }
        }
    }

    private abstract class SingleScheduleViewHolder(itemView: View, val actionListener: ActionListener)
        : BaseViewHolder(itemView) {

        protected val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        protected val nameView: TextView = itemView.findViewById(R.id.nameView)
        protected val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        protected val scheduleView: TextView = itemView.findViewById(R.id.scheduleView)
    }

    interface ScheduleListener {
        fun onScheduleListener(items: List<Schedule>)
    }

    companion object {
        const val ITEM_TYPE_ALL_SCHEDULE = 0
        const val ITEM_TYPE_SINGLE_SCHEDULE_TODAY = 1
        const val ITEM_TYPE_SINGLE_SCHEDULE_TOMORROW = 2

        val callback = object : DiffUtil.ItemCallback<SubjectPackage>() {
            override fun areItemsTheSame(oldItem: SubjectPackage,
                                         newItem: SubjectPackage): Boolean {
                return oldItem.subject.subjectID == newItem.subject.subjectID
            }

            override fun areContentsTheSame(oldItem: SubjectPackage,
                                            newItem: SubjectPackage): Boolean {
                return oldItem == newItem
            }
        }
    }
}