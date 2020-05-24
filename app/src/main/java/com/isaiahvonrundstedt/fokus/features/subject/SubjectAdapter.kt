package com.isaiahvonrundstedt.fokus.features.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class SubjectAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Subject, SubjectAdapter.SubjectViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject_card,
            parent, false)
        return SubjectViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE)
    }

    inner class SubjectViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val nameView: AppCompatTextView = itemView.findViewById(R.id.nameView)
        private val descriptionView: AppCompatTextView = itemView.findViewById(R.id.descriptionView)
        private val dateTimeView: AppCompatTextView = itemView.findViewById(R.id.dateTimeView)

        fun onBind(subject: Subject) {
            rootView.setOnClickListener {
                actionListener.onActionPerformed(subject, ActionListener.Action.SELECT)
            }

            tagView.setImageDrawable(subject.tintDrawable(tagView.drawable))

            nameView.text = subject.code
            descriptionView.text = subject.description

            val builder = StringBuilder()
            val selectedDays = Subject.getDays(subject.daysOfWeek)
            selectedDays.forEachIndexed { index, dayOfWeek ->
                builder.append(itemView.context.getString(Subject.getDayNameResource(dayOfWeek)))

                if (index == selectedDays.size - 2)
                    builder.append(itemView.context.getString(R.string.and))
                else if (index < selectedDays.size - 2)
                    builder.append(", ")
            }
            builder.append(", ")
            builder.append(subject.formatStartTime())
                .append(" - ")
                .append(subject.formatEndTime())
            dateTimeView.text = builder.toString()

        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<Subject>() {
            override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                return oldItem == newItem
            }
        }
    }
}