package com.isaiahvonrundstedt.fokus.features.schedule.viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemScheduleBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class ScheduleViewerAdapter(items: List<Schedule>) : BaseAdapter<Schedule,
        ScheduleViewerAdapter.ScheduleViewHolder>(Schedule.DIFF_CALLBACK) {

    init {
        submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = LayoutItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ScheduleViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ScheduleViewHolder(itemView: View) : BaseAdapter.BaseViewHolder(itemView) {

        private val binding = LayoutItemScheduleBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is Schedule) {
                binding.titleView.text = t.formatDaysOfWeek(binding.root.context, false)
                binding.summaryView.text = t.formatBothTime(binding.root.context)
            }
        }
    }
}