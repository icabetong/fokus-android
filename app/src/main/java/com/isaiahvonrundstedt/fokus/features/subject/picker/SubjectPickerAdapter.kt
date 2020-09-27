package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class SubjectPickerAdapter(private val actionListener: ActionListener)
    : BaseAdapter<SubjectPackage, SubjectPickerAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject_selector,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    class ViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseViewHolder(itemView) {

        private val tagView: ImageView = itemView.findViewById(R.id.tagView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    tagView.setImageDrawable(tintDrawable(tagView.drawable))
                    titleView.text = code
                    summaryView.text = description
                }
                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT, emptyMap())
                }
            }
        }
    }

    companion object {
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