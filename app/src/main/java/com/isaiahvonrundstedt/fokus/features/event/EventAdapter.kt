package com.isaiahvonrundstedt.fokus.features.event

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.extensions.addStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class EventAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Event, EventAdapter.EventViewHolder>(callback) {

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_event,
            parent, false)
        return EventViewHolder(rowView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).schedule!!.isBeforeNow) viewTypeFinished
            else viewTypePending
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val locationView: AppCompatTextView = itemView.findViewById(R.id.locationView)
        private val nameView: AppCompatTextView = itemView.findViewById(R.id.nameView)
        private val dayView: AppCompatTextView = itemView.findViewById(R.id.dayView)
        private val timeView: AppCompatTextView = itemView.findViewById(R.id.timeView)

        fun onBind(event: Event) {
            locationView.text = event.location
            nameView.text = event.name
            dayView.text = event.formatScheduleDate(rootView.context)
            timeView.text = event.formatScheduleTime()
            rootView.setOnClickListener {
                actionListener.onActionPerformed(event, ActionListener.Action.SELECT)
            }

            if (event.schedule!!.isBeforeNow)
                nameView.addStrikeThroughEffect()
        }
    }

    companion object {
        const val viewTypePending: Int = 0
        const val viewTypeFinished: Int = 1

        val callback = object: DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }

}