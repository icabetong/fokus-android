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
import com.isaiahvonrundstedt.fokus.components.delegates.SwipeDelegate
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter

class SubjectAdapter(private var actionListener: ActionListener)
    : BaseListAdapter<SubjectResource, SubjectAdapter.SubjectViewHolder>(callback), SwipeDelegate {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject,
            parent, false)
        return SubjectViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
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
            with(t) {
                if (this is SubjectResource) {
                    with(this.subject) {
                        nameView.transitionName = SubjectEditor.TRANSITION_ID_CODE + subjectID
                        descriptionView.transitionName = SubjectEditor.TRANSITION_ID_DESCRIPTION + subjectID

                        tagView.setImageDrawable(tintDrawable(tagView.drawable))
                        nameView.text = code
                        descriptionView.text = description
                    }

                    if (scheduleView.childCount > 0)
                        scheduleView.removeAllViews()
                    schedules.forEach {
                        val chip = Chip(itemView.context).apply {
                            text = it.format(context)
                        }
                        scheduleView.addView(chip)
                    }

                    rootView.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.SELECT,
                            mapOf(SubjectEditor.TRANSITION_ID_CODE + subject.subjectID to nameView,
                                SubjectEditor.TRANSITION_ID_DESCRIPTION + subject.subjectID to descriptionView))
                    }
                }
            }
        }
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<SubjectResource>() {
            override fun areItemsTheSame(oldItem: SubjectResource,
                                         newItem: SubjectResource): Boolean {
                return oldItem.subject.subjectID == newItem.subject.subjectID
            }

            override fun areContentsTheSame(oldItem: SubjectResource,
                                            newItem: SubjectResource): Boolean {
                return oldItem == newItem
            }
        }
    }
}