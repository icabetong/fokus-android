package com.isaiahvonrundstedt.fokus.features.attachments

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
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
        holder.onBind(getItem(position))
    }

    class ViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseViewHolder(itemView) {

        private val iconView: AppCompatImageView = itemView.findViewById(R.id.iconView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val removeButton: Chip = itemView.findViewById(R.id.removeButton)

        override fun <T> onBind(t: T) {
            if (t is Attachment) {
                titleView.text = when (t.type) {
                    Attachment.TYPE_IMPORTED_FILE ->
                        t.target?.let { File(it) }?.name
                    Attachment.TYPE_WEBSITE_LINK ->
                        t.name ?: t.target
                    Attachment.TYPE_CONTENT_URI ->
                        t.name ?: Uri.parse(t.target).getFileName(itemView.context)
                    else ->
                        t.target
                }

                iconView.setImageResource(t.getIconResource())

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