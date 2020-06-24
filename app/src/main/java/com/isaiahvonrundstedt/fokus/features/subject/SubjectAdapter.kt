package com.isaiahvonrundstedt.fokus.features.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class SubjectAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Subject, SubjectAdapter.SubjectViewHolder>(callback) {

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
        private val dateTimeView: TextView = itemView.findViewById(R.id.dateTimeView)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is Subject) {
                    nameView.transitionName = transitionCodeID + id
                    descriptionView.transitionName = transitionDescriptionID + id

                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    nameView.text = code
                    descriptionView.text = description
                    dateTimeView.text = formatSchedule(rootView.context)

                    rootView.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.SELECT,
                            mapOf(transitionCodeID + id to nameView,
                                transitionDescriptionID + id to descriptionView))
                    }
                }
            }
        }
    }

    companion object {
        const val transitionCodeID = "transition:code:"
        const val transitionDescriptionID = "transition:description:"

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