package com.isaiahvonrundstedt.fokus.features.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemScheduleEditorBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter

class ScheduleAdapter(private val actionListener: ActionListener<Schedule>)
    : BaseBasicAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = LayoutItemScheduleEditorBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ScheduleViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.onBind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ScheduleViewHolder(itemView: View)
        : BaseBasicAdapter.BaseBasicViewHolder<Schedule>(itemView) {

        private val binding = LayoutItemScheduleEditorBinding.bind(itemView)

        override fun onBind(t: Schedule, position: Int) {
            with(binding) {
                titleView.text = t.formatDaysOfWeek(root.context, false)
                summaryView.text = t.formatBothTime(root.context)

                removeButton.setOnClickListener {
                    actionListener.onActionPerformed(t, position, ActionListener.Action.DELETE)
                }

                root.setOnClickListener {
                    actionListener.onActionPerformed(t, position, ActionListener.Action.SELECT)
                }
            }
        }
    }
}