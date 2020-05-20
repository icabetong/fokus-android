package com.isaiahvonrundstedt.fokus.features.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class NotificationAdapter(private var swipeListener: SwipeListener)
    : BaseAdapter<Notification, NotificationAdapter.ViewHolder>(callback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_notification,
            parent, false)
        return ViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    override fun onSwipe(position: Int, direction: Int) {
        swipeListener.onSwipePerformed(position, getItem(position), direction)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val iconView: AppCompatImageView = itemView.findViewById(R.id.iconView)
        private val titleView: AppCompatTextView = itemView.findViewById(R.id.titleView)
        private val summaryView: AppCompatTextView = itemView.findViewById(R.id.summaryView)
        private val dateTimeView: AppCompatTextView = itemView.findViewById(R.id.dateTimeView)

        fun onBind(notification: Notification) {
            titleView.text = notification.title
            summaryView.text = notification.content
            iconView.setImageDrawable(notification.getIconDrawable(rootView.context))
            dateTimeView.text = notification.formatDateTime()
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }
        }
    }
}