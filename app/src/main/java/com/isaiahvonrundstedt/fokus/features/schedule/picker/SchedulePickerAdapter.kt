package com.isaiahvonrundstedt.fokus.features.schedule.picker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import java.time.DayOfWeek

class SchedulePickerAdapter(private val actionListener: ActionListener)
    : BaseAdapter<Schedule, SchedulePickerAdapter.ViewHolder>(Schedule.DIFF_CALLBACK) {

    private val itemList = mutableListOf<Schedule>()

    fun setItems(items: List<Schedule>) {
        itemList.clear()
        items.forEach {
            it.getDaysAsList().forEach { day ->
                if (day <= DayOfWeek.SUNDAY.value) {
                    val newSchedule = Schedule(startTime = it.startTime,
                        endTime = it.endTime)
                    newSchedule.daysOfWeek = day
                    itemList.add(newSchedule)
                }
            }
        }
        submitList(itemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_schedule,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ViewHolder(itemView: View, private val actionListener: ActionListener) : BaseViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)

        override fun <T> onBind(t: T) {
            if (t is Schedule) {
                titleView.text = itemView.context.getString(t.getStringResourceForDay(t.daysOfWeek))
                summaryView.text = t.formatBothTime()
            }

            rootView.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.SELECT, emptyMap())
            }
        }
    }
}