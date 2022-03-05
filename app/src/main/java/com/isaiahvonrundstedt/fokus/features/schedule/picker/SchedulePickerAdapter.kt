package com.isaiahvonrundstedt.fokus.features.schedule.picker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemScheduleBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import java.time.DayOfWeek

class SchedulePickerAdapter(private val actionListener: ActionListener) :
    BaseAdapter<Schedule, SchedulePickerAdapter.ViewHolder>(Schedule.DIFF_CALLBACK) {

    private val itemList = mutableListOf<Schedule>()

    fun setItems(items: List<Schedule>) {
        itemList.clear()
        items.forEach {
            it.parseDaysOfWeek().forEach { day ->
                if (day <= DayOfWeek.SUNDAY.value) {
                    val newSchedule = Schedule(
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                    newSchedule.daysOfWeek = day
                    itemList.add(newSchedule)
                }
            }
        }
        submitList(itemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        private val binding = LayoutItemScheduleBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is Schedule) {
                binding.titleView.text =
                    binding.root.context.getString(t.getStringResourceForDay(t.daysOfWeek))
                binding.summaryView.text = t.formatBothTime(binding.root.context)
            }

            binding.root.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.SELECT, null)
            }
        }
    }
}