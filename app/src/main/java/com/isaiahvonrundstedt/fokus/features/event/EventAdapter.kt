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
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class EventAdapter(private var actionListener: ActionListener)
    : BaseAdapter<Event, RecyclerView.ViewHolder>(callback) {

    override fun onSwipe(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.START)
            actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val resID: Int = if (viewType == viewTypePending) R.layout.layout_item_event
            else R.layout.layout_item_event_finished

        val rowView: View = LayoutInflater.from(parent.context).inflate(resID, parent, false)
        return if (viewType == viewTypePending) PendingViewHolder(rowView)
            else FinishedViewHolder(rowView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).schedule!!.isBeforeNow) viewTypeFinished
            else viewTypePending
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == viewTypeFinished)
            (holder as FinishedViewHolder).onBind(getItem(position))
        else (holder as PendingViewHolder).onBind(getItem(position))
    }

    inner class FinishedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val nameView: AppCompatTextView = itemView.findViewById(R.id.nameView)

        fun onBind(event: Event) {
            nameView.text = event.name
            nameView.paintFlags = nameView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            rootView.setOnClickListener {
                actionListener.onActionPerformed(event, ActionListener.Action.SELECT)
            }
        }
    }

    inner class PendingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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