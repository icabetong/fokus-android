package com.isaiahvonrundstedt.fokus.features.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemLogBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class LogAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Log, LogAdapter.ViewHolder>(Log.DIFF_CALLBACK), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemLogBinding.inflate(LayoutInflater.from(parent.context), parent,
            false)
        return ViewHolder(binding.root)
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

        private val binding = LayoutItemLogBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is Log) {
                binding.titleView.text = t.title
                binding.summaryView.text = t.content
                binding.dateTimeView.text = t.formatDateTime(binding.root.context)
                t.setIconToView(binding.iconView)
            }
        }
    }
}