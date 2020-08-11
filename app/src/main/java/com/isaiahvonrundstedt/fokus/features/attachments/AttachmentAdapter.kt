package com.isaiahvonrundstedt.fokus.features.attachments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.components.extensions.getIndexByID
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class AttachmentAdapter(private var actionListener: ActionListener)
    : BaseAdapter<AttachmentAdapter.ViewHolder>() {

    val itemList = mutableListOf<Attachment>()

    fun setItems(items: List<Attachment>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun <T> insert(t: T) {
        if (t is Attachment) {
            itemList.add(t)
            val index = itemList.indexOf(t)

            notifyItemInserted(index)
            notifyItemRangeChanged(index, itemList.size)
        }
    }

    override fun <T> remove(t: T) {
        if (t is Attachment) {
            val index = itemList.indexOf(t)

            itemList.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeRemoved(index, itemList.size)
        }
    }

    override fun <T> update(t: T) {
        if (t is Attachment) {
            val index = itemList.getIndexByID(t.attachmentID)
            if (index != -1) {
                itemList[index] = t
                notifyItemChanged(index)
                notifyItemRangeChanged(index, itemList.size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_attachment,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(itemList[holder.adapterPosition])
    }

    class ViewHolder(itemView: View, actionListener: ActionListener)
        : BaseViewHolder(itemView, actionListener) {

        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val removeButton: Chip = itemView.findViewById(R.id.removeButton)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is Attachment) {
                    titleView.text = uri?.getFileName(itemView.context)

                    rootView.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.SELECT)
                    }

                    removeButton.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.DELETE)
                    }
                }
            }
        }
    }
}