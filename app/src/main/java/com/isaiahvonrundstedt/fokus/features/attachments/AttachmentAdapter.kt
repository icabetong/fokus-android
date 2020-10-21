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
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemAttachmentBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemEventBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import java.io.File

class AttachmentAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Attachment, AttachmentAdapter.ViewHolder>(Attachment.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemAttachmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(itemView: View): BaseViewHolder(itemView) {

        private val binding = LayoutItemAttachmentBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is Attachment) {
                binding.titleView.text = when (t.type) {
                    Attachment.TYPE_IMPORTED_FILE ->
                        t.target?.let { File(it) }?.name
                    Attachment.TYPE_WEBSITE_LINK ->
                        t.name ?: t.target
                    Attachment.TYPE_CONTENT_URI ->
                        t.name ?: Uri.parse(t.target).getFileName(itemView.context)
                    else ->
                        t.target
                }

                binding.iconView.setImageResource(t.getIconResource())

                binding.removeButton.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.DELETE,
                        emptyMap())
                }

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT,
                        emptyMap())
                }
            }
        }
    }
}