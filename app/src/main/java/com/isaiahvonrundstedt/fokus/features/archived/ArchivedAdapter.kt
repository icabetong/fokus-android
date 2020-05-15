package com.isaiahvonrundstedt.fokus.features.archived

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.Task
import java.util.*

class ArchivedAdapter(private var actionListener: ActionListener,
                      private var swipeListener: SwipeListener)
    : BaseAdapter<ArchivedAdapter.ArchivedViewHolder>() {

    private var itemList = ArrayList<Core>()

    fun setObservableItems(items: List<Core>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_archived,
            parent, false)
        return ArchivedViewHolder(rowView)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ArchivedViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    override fun onSwipe(position: Int, direction: Int) {
        swipeListener.onSwipePerformed(position, itemList[position], direction)
    }

    inner class ArchivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val subjectNameView: AppCompatTextView = itemView.findViewById(R.id.subjectNameView)
        private val taskNameView: AppCompatTextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: AppCompatTextView = itemView.findViewById(R.id.dueDateView)
        private val tagView: View = itemView.findViewById(R.id.tagView)

        fun onBind(core: Core) {
            subjectNameView.text = core.subject.description ?: core.subject.code
            tagView.setBackgroundColor(core.subject.tag.actualColor)

            taskNameView.text = core.task.name
            dueDateView.text = Task.formatDueDate(itemView.context, core.task.dueDate!!)

            rootView.setOnClickListener {
                actionListener.onActionPerformed(core, ActionListener.Action.SELECT)
            }
        }
    }
}