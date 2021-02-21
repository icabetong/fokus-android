package com.isaiahvonrundstedt.fokus.features.attachments

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemAttachmentBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter
import java.io.File

class AttachmentAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Attachment, AttachmentAdapter.AttachmentViewHolder>(Attachment.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding = LayoutItemAttachmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AttachmentViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val t: Attachment = getItem(position)
        val binding = holder.binding

        with (binding) {
            titleView.text = when (t.type) {
                Attachment.TYPE_IMPORTED_FILE ->
                    t.name ?: t.target
                Attachment.TYPE_WEBSITE_LINK ->
                    t.name ?: t.target
                Attachment.TYPE_CONTENT_URI ->
                    t.name ?: Uri.parse(t.target).getFileName(binding.root.context)
                else ->
                    t.target
            }

            iconView.setImageResource(t.getIconResource())

            removeButton.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.DELETE, null)
            }

            root.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.SELECT, null)
            }
        }
    }

    class AttachmentViewHolder(itemView: View): BaseViewHolder(itemView) {
        val binding = LayoutItemAttachmentBinding.bind(itemView)

        override fun <T> onBind(t: T) {}
    }
}