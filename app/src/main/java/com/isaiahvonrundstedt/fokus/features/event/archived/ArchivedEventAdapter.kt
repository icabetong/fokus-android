package com.isaiahvonrundstedt.fokus.features.event.archived

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemArchivedEventBinding
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment

class ArchivedEventAdapter(private val listener: SelectListener) :
    BaseAdapter<EventPackage, ArchivedEventAdapter.ArchivedEventViewHolder>(EventPackage.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedEventViewHolder {
        val binding = LayoutItemArchivedEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ArchivedEventViewHolder(binding.root, listener)
    }

    override fun onBindViewHolder(holder: ArchivedEventViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ArchivedEventViewHolder(
        itemView: View,
        private val listener: SelectListener
    ) : BaseViewHolder(itemView) {
        private val binding = LayoutItemArchivedEventBinding.bind(itemView)

        override fun <T> onBind(data: T) {
            if (data is EventPackage) {
                with(data.event) {
                    binding.root.transitionName = BaseFragment.TRANSITION_ELEMENT_ROOT + eventID

                    binding.locationView.text = location
                    binding.nameView.text = name
                    binding.timeView.text = formatScheduleTime(binding.root.context)
                }

                binding.root.setOnClickListener {
                    listener.onItemSelected(data)
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
            }
        }
    }
}