package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemSubjectPickerBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class SubjectPickerAdapter(private val actionListener: ActionListener) :
    BaseAdapter<SubjectPackage, SubjectPickerAdapter.ViewHolder>(SubjectPackage.DIFF_CALLBACK),
    Swipeable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemSubjectPickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        actionListener.onActionPerformed(getItem(position), ActionListener.Action.DELETE, null)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        private val binding = LayoutItemSubjectPickerBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    binding.root.transitionName = subjectID
                    binding.tagView.setImageDrawable(tintDrawable(binding.tagView.drawable))
                    binding.titleView.text = code
                    binding.summaryView.text = description
                }

                binding.root.setOnClickListener {
                    actionListener.onActionPerformed(t, ActionListener.Action.SELECT, null)
                }
            }
        }
    }
}