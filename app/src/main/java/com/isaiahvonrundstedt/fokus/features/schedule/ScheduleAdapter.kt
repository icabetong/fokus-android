package com.isaiahvonrundstedt.fokus.features.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.getIndexByID
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter

class ScheduleAdapter(private val actionListener: ActionListener)
    : BaseAdapter<ScheduleAdapter.ViewHolder>() {

    val itemList = mutableListOf<Schedule>()

    fun setItems(items: List<Schedule>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun <T> insert(t: T) {
        if (t is Schedule) {
            itemList.add(t)
            val index = itemList.indexOf(t)

            notifyItemInserted(index)
            notifyItemRangeInserted(index, itemList.size)
        }
    }

    override fun <T> remove(t: T) {
        if (t is Schedule) {
            val index = itemList.indexOf(t)

            itemList.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeRemoved(index, itemList.size)
        }
    }

    override fun <T> update(t: T) {
        if (t is Schedule) {
            val index = itemList.getIndexByID(t.scheduleID)

            if (index != -1) {
                itemList[index] = t
                notifyItemChanged(index)
                notifyItemRangeChanged(index, itemList.size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_schedule,
            parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(itemList[holder.adapterPosition])
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(itemView: View, actionListener: ActionListener)
        : BaseAdapter.BaseViewHolder(itemView, actionListener) {

        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)
        private val removeButton: Chip = itemView.findViewById(R.id.removeButton)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is Schedule) {
                    titleView.text = formatDaysOfWeek(rootView.context)
                    summaryView.text = formatBothTime()
                }
                removeButton.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.DELETE)
                }

                rootView.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT)
                }
            }
        }
    }
}