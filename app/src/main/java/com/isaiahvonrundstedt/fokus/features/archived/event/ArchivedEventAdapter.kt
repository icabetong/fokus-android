package com.isaiahvonrundstedt.fokus.features.archived.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemArchivedEventBinding
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor

class ArchivedEventAdapter:
    BaseAdapter<EventPackage, ArchivedEventAdapter.ArchivedEventViewHolder>(EventPackage.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedEventViewHolder {
        val binding = LayoutItemArchivedEventBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ArchivedEventViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ArchivedEventViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ArchivedEventViewHolder(itemView: View): BaseViewHolder(itemView) {
        private val binding = LayoutItemArchivedEventBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is EventPackage) {
                with(t.event) {
                    binding.root.transitionName = BaseEditor.TRANSITION_ELEMENT_ROOT + eventID

                    binding.locationView.text = location
                    binding.nameView.text = name
                    binding.timeView.text = formatScheduleTime(binding.root.context)
                }

                if (t.subject != null) {
                    with(binding.subjectView) {
                        text = t.subject?.code
                        setCompoundDrawableAtStart(t.subject?.tintDrawable(getCompoundDrawableAtStart()))
                    }
                } else binding.subjectView.isVisible = false
            }
        }
    }
}