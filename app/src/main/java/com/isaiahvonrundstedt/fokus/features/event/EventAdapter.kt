package com.isaiahvonrundstedt.fokus.features.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.delegates.SwipeDelegate
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter

class EventAdapter(private var actionListener: ActionListener)
    : BaseListAdapter<EventResource, EventAdapter.EventViewHolder>(callback), SwipeDelegate {

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
        : BaseViewHolder(itemView) {

        private val locationView: TextView = itemView.findViewById(R.id.locationView)
        private val subjectView: TextView = itemView.findViewById(R.id.subjectNameView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)
        private val dayView: TextView = itemView.findViewById(R.id.dayView)
        private val timeView: TextView = itemView.findViewById(R.id.timeView)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is EventResource) {
                    nameView.transitionName = EventEditor.TRANSITION_ID_NAME + event.eventID

                    with(event) {
                        locationView.text = location
                        nameView.text = name
                        dayView.text = formatScheduleDate(itemView.context)
                        timeView.text = formatScheduleTime()

                        schedule?.isBeforeNow?.let {
                            nameView.setStrikeThroughEffect(it)
                            if (it)
                                nameView.setTextColorFromResource(R.color.color_secondary_text)
                        }
                    }

                    subjectView.isVisible = subject != null
                    subject?.let {
                        with(subjectView) {
                            text = it.code
                            setCompoundDrawableAtStart(it.tintDrawable(getCompoundDrawableAtStart()))
                        }
                    }

                    rootView.setOnClickListener {
                        actionListener.onActionPerformed(this, ActionListener.Action.SELECT,
                            mapOf(EventEditor.TRANSITION_ID_NAME + event.eventID to nameView)
                        )
                    }
                }
            }
        }
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<EventResource>() {
            override fun areItemsTheSame(oldItem: EventResource, newItem: EventResource): Boolean {
                return oldItem.event.eventID == newItem.event.eventID
            }

            override fun areContentsTheSame(oldItem: EventResource,
                                            newItem: EventResource): Boolean {
                return oldItem == newItem
            }
        }
    }

}