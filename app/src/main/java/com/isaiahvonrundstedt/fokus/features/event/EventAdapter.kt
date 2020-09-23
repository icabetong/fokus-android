package com.isaiahvonrundstedt.fokus.features.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setStrikeThroughEffect
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isBeforeNow
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.features.event.editor.EventEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter

class EventAdapter(private var actionListener: ActionListener)
    : BaseListAdapter<EventPackage, EventAdapter.EventViewHolder>(callback), Swipeable {

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
        private val timeView: TextView = itemView.findViewById(R.id.timeView)

        override fun <T> onBind(t: T) {
            with(t) {
                if (this is EventPackage) {
                    nameView.transitionName = EventEditor.TRANSITION_ID_NAME + event.eventID

                    with(event) {
                        locationView.text = location
                        nameView.text = name
                        timeView.text = formatScheduleTime()
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
        const val VIEW_TYPE_TIME = 0
        const val VIEW_TYPE_DATE_TIME = 1

        val callback = object : DiffUtil.ItemCallback<EventPackage>() {
            override fun areItemsTheSame(oldItem: EventPackage, newItem: EventPackage): Boolean {
                return oldItem.event.eventID == newItem.event.eventID
            }

            override fun areContentsTheSame(oldItem: EventPackage,
                                            newItem: EventPackage): Boolean {
                return oldItem == newItem
            }
        }
    }

}