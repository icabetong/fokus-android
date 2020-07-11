package com.isaiahvonrundstedt.fokus.features.subject.selector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectResource

class SubjectSelectorAdapter(private val actionListener: ActionListener)
    : BaseListAdapter<SubjectResource, SubjectSelectorAdapter.ViewHolder>(callback) {

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
        : BaseViewHolder(itemView) {

        private val tagView: ImageView = itemView.findViewById(R.id.tagView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)

        override fun <T> onBind(t: T) {
            if (t is SubjectResource) {
                with(t.subject) {
                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    titleView.text = code
                    summaryView.text = description
                }
                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t.subject, ActionListener.Action.SELECT, emptyMap())
                }
            }
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<SubjectResource>() {
            override fun areItemsTheSame(oldItem: SubjectResource, newItem: SubjectResource): Boolean {
                return oldItem.subject.subjectID == newItem.subject.subjectID
            }

            override fun areContentsTheSame(oldItem: SubjectResource, newItem: SubjectResource): Boolean {
                return oldItem == newItem
            }
        }
    }

}