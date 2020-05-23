package com.isaiahvonrundstedt.fokus.features.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class EventAdapter(private var actionListener: ActionListener,
                   private var swipeListener: SwipeListener)
    : BaseAdapter<Event, EventAdapter.EventViewHolder>(callback) {

    override fun onSwipe(position: Int, direction: Int) {
        swipeListener.onSwipePerformed(position, getItem(position), direction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_event,
            parent, false)
        return EventViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val locationView: AppCompatTextView = itemView.findViewById(R.id.locationView)
        private val nameView: AppCompatTextView = itemView.findViewById(R.id.nameView)
        private val scheduleView: AppCompatTextView = itemView.findViewById(R.id.scheduleView)

        fun onBind(event: Event) {
            locationView.text = event.location
            nameView.text = event.name
            scheduleView.text = event.formatSchedule(rootView.context)
            rootView.setOnClickListener {
                actionListener.onActionPerformed(event, ActionListener.Action.SELECT)
            }
        }
    }

    companion object {
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