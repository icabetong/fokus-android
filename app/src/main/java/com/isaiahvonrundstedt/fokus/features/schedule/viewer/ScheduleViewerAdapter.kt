package com.isaiahvonrundstedt.fokus.features.schedule.viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class ScheduleViewerAdapter(items: List<Schedule>): BaseAdapter<Schedule,
        ScheduleViewerAdapter.ScheduleViewHolder>(Schedule.DIFF_CALLBACK) {

    init {
        submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_schedule,
            parent, false)
        return ScheduleViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ScheduleViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)

        override fun <T> onBind(t: T) {
            if (t is Schedule) {
                titleView.text = t.formatDaysOfWeek(itemView.context, false)
                summaryView.text = t.formatBothTime()
            }
        }
    }
}