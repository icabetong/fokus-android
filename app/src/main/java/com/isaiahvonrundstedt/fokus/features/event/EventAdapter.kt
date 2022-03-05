package com.isaiahvonrundstedt.fokus.features.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemEventBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment

class EventAdapter(
    private val actionListener: ActionListener,
    private val archiveListener: ArchiveListener
) : BaseAdapter<EventPackage, EventAdapter.EventViewHolder>(EventPackage.DIFF_CALLBACK), Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = LayoutItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return EventViewHolder(binding.root, actionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        when (direction) {
            ItemTouchHelper.START ->
                actionListener.onActionPerformed(
                    getItem(position), ActionListener.Action.DELETE,
                    null
                )
            ItemTouchHelper.END ->
                archiveListener.onItemArchive(getItem(position))
        }
    }

    class EventViewHolder(
        itemView: View,
        private val actionListener: ActionListener
    ) : BaseViewHolder(itemView) {
        private val binding = LayoutItemEventBinding.bind(itemView)

        override fun <T> onBind(data: T) {
            if (data is EventPackage) {
                with(data.event) {
                    binding.root.transitionName =
                        BaseFragment.TRANSITION_ELEMENT_ROOT + data.event.eventID

                    binding.locationView.text = location
                    binding.nameView.text = name
                    binding.timeView.text = formatScheduleTime(binding.root.context)
                }

                if (data.subject != null) {
                    with(binding.subjectView) {
                        text = data.subject?.code
                        setCompoundDrawableAtStart(
                            data.subject?.tintDrawable(
                                getCompoundDrawableAtStart()
                            )
                        )
                    }
                } else binding.subjectView.isVisible = false

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(data, ActionListener.Action.SELECT, it)
                }
            }
        }
    }
}