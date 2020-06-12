package com.isaiahvonrundstedt.fokus.features.shared.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.subject.Subject

class SubjectListAdapter(private val listener: ItemSelected)
    : ListAdapter<Subject, SubjectListAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject,
            parent, false)
        return ViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    interface ItemSelected {
        fun onItemSelected(subject: Subject)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val titleView: AppCompatTextView = itemView.findViewById(R.id.titleView)
        private val summaryView: AppCompatTextView = itemView.findViewById(R.id.summaryView)

        fun onBind(s: Subject) {
            rootView.setOnClickListener { listener.onItemSelected(s) }
            titleView.text = s.code
            summaryView.text = s.description

            tagView.setImageDrawable(s.tintDrawable(tagView.drawable))
        }
    }

    companion object {
        private var callback = object: DiffUtil.ItemCallback<Subject>() {
            override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}