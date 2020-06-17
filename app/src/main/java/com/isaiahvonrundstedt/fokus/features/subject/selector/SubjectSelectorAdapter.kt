package com.isaiahvonrundstedt.fokus.features.subject.selector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject

class SubjectSelectorAdapter(private val actionListener: ActionListener)
    : BaseAdapter<Subject, SubjectSelectorAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject_selector,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    override fun onSwipe(position: Int, direction: Int) {}

    class ViewHolder(itemView: View, private val actionListener: ActionListener)
        : RecyclerView.ViewHolder(itemView) {

        private val rootView: FrameLayout = itemView.findViewById(R.id.rootView)
        private val tagView: ImageView = itemView.findViewById(R.id.tagView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)

        fun onBind(subject: Subject) {
            with(subject) {
                tagView.setImageDrawable(tintDrawable(tagView.drawable))
                titleView.text = code
                summaryView.text = description
            }
            rootView.setOnClickListener {
                actionListener.onActionPerformed(subject, ActionListener.Action.SELECT, emptyMap())
            }
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