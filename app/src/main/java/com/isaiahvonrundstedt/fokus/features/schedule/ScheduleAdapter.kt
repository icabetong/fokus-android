package com.isaiahvonrundstedt.fokus.features.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemScheduleEditorBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class ScheduleAdapter(private val actionListener: ActionListener)
    : BaseAdapter<Schedule, ScheduleAdapter.ViewHolder>(Schedule.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemScheduleEditorBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {

        private val binding = LayoutItemScheduleEditorBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is Schedule) {
                binding.titleView.text = t.formatDaysOfWeek(binding.root.context, false)
                binding.summaryView.text = t.formatBothTime()
            }

            binding.removeButton.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.DELETE, emptyMap())
            }

            binding.root.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.SELECT, emptyMap())
            }
        }
    }
}