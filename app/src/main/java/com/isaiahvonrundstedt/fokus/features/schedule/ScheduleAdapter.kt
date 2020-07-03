package com.isaiahvonrundstedt.fokus.features.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.getIndexByID
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class ScheduleAdapter(private val actionListener: BaseAdapter.ActionListener)
    : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    val itemList = ArrayList<Schedule>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_schedule,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(itemList[holder.adapterPosition])
    }

    override fun getItemCount(): Int = itemList.size

    fun setItems(items: List<Schedule>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    fun insert(schedule: Schedule) {
        itemList.add(schedule)
        val index = itemList.indexOf(schedule)

        notifyItemInserted(index)
        notifyItemRangeInserted(index, itemList.size)
    }

    fun remove(schedule: Schedule) {
        notifyItemRemoved(itemList.indexOf(schedule))
        notifyItemRangeRemoved(itemList.indexOf(schedule), itemList.size)
        itemList.remove(schedule)
    }

    fun update(schedule: Schedule) {
        val index = itemList.getIndexByID(schedule.scheduleID)
        if (index != -1) {
            itemList[index] = schedule
            notifyItemChanged(index)
            notifyItemRangeChanged(index, itemList.size)
        }
    }

    class ViewHolder(itemView: View, private var listener: BaseAdapter.ActionListener): BaseAdapter.BaseViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is Schedule) {
                    titleView.text = formatDaysOfWeek(rootView.context)
                    summaryView.text = formatBothTime()
                }
                removeButton.setOnClickListener {
                    listener.onActionPerformed(t, BaseAdapter.ActionListener.Action.DELETE,
                        emptyMap())
                }
                rootView.setOnClickListener {
                    listener.onActionPerformed(t, BaseAdapter.ActionListener.Action.SELECT,
                        emptyMap()) }
            }
        }
    }
}