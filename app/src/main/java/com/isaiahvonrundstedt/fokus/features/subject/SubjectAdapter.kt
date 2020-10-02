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
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor

class SubjectAdapter(private var actionListener: ActionListener)
    : BaseAdapter<SubjectPackage, BaseAdapter.BaseViewHolder>(callback), Swipeable {

    var currentOption = SubjectViewModel.FilterOption.TODAY

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == ITEM_TYPE_ALL_SCHEDULE) {
            val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject,
                parent, false)
            SubjectViewHolder(rowView, actionListener)
        } else {
            val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject_today,
                parent, false)
            SubjectTodayViewHolder(rowView, actionListener)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentOption) {
            SubjectViewModel.FilterOption.ALL -> ITEM_TYPE_ALL_SCHEDULE
            SubjectViewModel.FilterOption.TODAY -> ITEM_TYPE_TODAY_ONLY
        }
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
    }

    class SubjectViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseViewHolder(itemView) {

        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)
        private val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        private val scheduleView: ChipGroup = itemView.findViewById(R.id.scheduleView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    nameView.transitionName = SubjectEditor.TRANSITION_ID_CODE + subjectID
                    descriptionView.transitionName = SubjectEditor.TRANSITION_ID_DESCRIPTION + subjectID

                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    nameView.text = code
                    descriptionView.text = description
                }

                if (scheduleView.childCount > 0)
                    scheduleView.removeAllViews()
                t.schedules.forEach {
                    val chip = Chip(itemView.context).apply {
                        text = it.format(context, true)
                    }
                    scheduleView.addView(chip)
                }

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT,
                        mapOf(SubjectEditor.TRANSITION_ID_CODE + t.subject.subjectID to nameView,
                            SubjectEditor.TRANSITION_ID_DESCRIPTION + t.subject.subjectID to descriptionView))
                }
            }
        }
    }

    class SubjectTodayViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseViewHolder(itemView) {

        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)
        private val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        private val scheduleView: TextView = itemView.findViewById(R.id.scheduleView)

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

    companion object {
        const val ITEM_TYPE_ALL_SCHEDULE = 0
        const val ITEM_TYPE_TODAY_ONLY = 1

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