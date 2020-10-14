package com.isaiahvonrundstedt.fokus.features.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class LogAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Log, LogAdapter.ViewHolder>(Log.DIFF_CALLBACK), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_log, parent, false)
        return ViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
    }

    class ViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val iconView: AppCompatImageView = itemView.findViewById(R.id.iconView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)
        private val dateTimeView: TextView = itemView.findViewById(R.id.dateTimeView)

        override fun <T> onBind(t: T) {
            if (t is Log) {
                titleView.text = t.title
                summaryView.text = t.content
                dateTimeView.text = t.formatDateTime()
                t.setIconToView(iconView)
            }
        }
    }
}