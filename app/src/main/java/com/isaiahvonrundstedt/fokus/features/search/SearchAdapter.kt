package com.isaiahvonrundstedt.fokus.features.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class SearchAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Core, SearchAdapter.SearchViewHolder>(callback) {

    override fun onSwipe(position: Int, direction: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_result,
            parent, false)
        return SearchViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    inner class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val overlineView: AppCompatTextView = itemView.findViewById(R.id.overlineView)
        private val titleView: AppCompatTextView = itemView.findViewById(R.id.titleView)
        private val summaryView: AppCompatTextView = itemView.findViewById(R.id.summaryView)

        fun onBind(core: Core) {
            rootView.setOnClickListener {
                actionListener.onActionPerformed(core, ActionListener.Action.SELECT)
            }

            val resId = if (core.task.isFinished) R.string.status_task_finished
                else R.string.status_task_pending

            overlineView.text = rootView.context.getString(resId)
            titleView.text = core.task.name
            summaryView.text  = core.subject.description ?: core.subject.code
            tagView.setImageDrawable(core.subject.tintDrawable(tagView.drawable))
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<Core>() {
            override fun areItemsTheSame(oldItem: Core, newItem: Core): Boolean {
                return oldItem.task.taskID == newItem.task.taskID
            }

            override fun areContentsTheSame(oldItem: Core, newItem: Core): Boolean {
                return oldItem == newItem
            }
        }
    }
}