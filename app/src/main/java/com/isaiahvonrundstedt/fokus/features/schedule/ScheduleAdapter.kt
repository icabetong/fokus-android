package com.isaiahvonrundstedt.fokus.features.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemScheduleEditorBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter

class ScheduleAdapter(private val actionListener: ActionListener)
    : BaseAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(Schedule.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = LayoutItemScheduleEditorBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ScheduleViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule: Schedule = getItem(position)

        with(holder.binding) {
            titleView.text = schedule.formatDaysOfWeek(root.context, false)
            summaryView.text = schedule.formatBothTime(root.context)

            removeButton.setOnClickListener {
                actionListener.onActionPerformed(schedule, ActionListener.Action.DELETE, null)
            }

            root.setOnClickListener {
                actionListener.onActionPerformed(schedule, ActionListener.Action.SELECT, null)
            }
        }
    }

    class ScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = LayoutItemScheduleEditorBinding.bind(itemView)
    }

}