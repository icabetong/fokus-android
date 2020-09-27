package com.isaiahvonrundstedt.fokus.features.attachments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import java.io.File

class AttachmentAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Attachment, AttachmentAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_attachment,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    class ViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val removeButton: Chip = itemView.findViewById(R.id.removeButton)

        override fun <T> onBind(t: T) {
            if (t is Attachment) {
                titleView.text = t.target?.let { File(it).name }

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT,
                        emptyMap())
                }

                removeButton.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.DELETE,
                        emptyMap())
                }
            }
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<Attachment>() {
            override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
                return oldItem.attachmentID == newItem.attachmentID
            }

        }
    }
}