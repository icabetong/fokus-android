package com.isaiahvonrundstedt.fokus.features.attachments

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemAttachmentBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter
import java.io.File

class AttachmentAdapter(private var actionListener: ActionListener<Attachment>)
    : BaseBasicAdapter<Attachment, AttachmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemAttachmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View): BaseBasicViewHolder<Attachment>(itemView) {

        private val binding = LayoutItemAttachmentBinding.bind(itemView)

        override fun onBind(t: Attachment, position: Int) {
            with (binding) {
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

                removeButton.setOnClickListener {
                    actionListener.onActionPerformed(t, position, ActionListener.Action.DELETE)
                }

                root.setOnClickListener {
                    actionListener.onActionPerformed(t, position, ActionListener.Action.SELECT)
                }
            }
        }
    }
}