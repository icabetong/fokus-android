package com.isaiahvonrundstedt.fokus.features.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.extensions.addStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class EventAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Event, EventAdapter.EventViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_event,
            parent, false)
        return EventViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE,
                emptyMap())
    }

    class EventViewHolder(itemView: View, private val actionListener: ActionListener)
        : RecyclerView.ViewHolder(itemView) {

        private val rootView: FrameLayout = itemView.findViewById(R.id.rootView)
        private val locationView: TextView = itemView.findViewById(R.id.locationView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)
        private val dayView: TextView = itemView.findViewById(R.id.dayView)
        private val timeView: TextView = itemView.findViewById(R.id.timeView)

        fun onBind(event: Event) {
            val id = event.id
            nameView.transitionName = transitionEventName + id
            locationView.transitionName = transitionLocation + id

            with(event) {
                locationView.text = location
                nameView.text = name
                dayView.text = event.formatScheduleDate(rootView.context)
                timeView.text = event.formatScheduleTime()

                if (event.schedule!!.isBeforeNow)
                    nameView.addStrikeThroughEffect()
            }

            rootView.setOnClickListener {
                actionListener.onActionPerformed(event, ActionListener.Action.SELECT,
                    mapOf(transitionEventName + id to nameView, transitionLocation + id to locationView))
            }
        }
    }

    companion object {
        const val transitionEventName = "transition:name:"
        const val transitionLocation = "transition:location:"

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