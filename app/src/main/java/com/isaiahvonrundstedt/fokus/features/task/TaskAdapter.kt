package com.isaiahvonrundstedt.fokus.features.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import org.joda.time.LocalDateTime
import java.util.*

class TaskAdapter(private var actionListener: ActionListener,
                  private var swipeListener: SwipeListener)
    : BaseAdapter<TaskAdapter.TaskViewHolder>() {

    private var itemList = ArrayList<Core>()

    fun setObservableItems(items: List<Core>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onSwipe(position: Int, direction: Int) {
        swipeListener.onSwipePerformed(position, itemList[position], direction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_task,
            parent, false)
        return TaskViewHolder(rowView)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    inner class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkBox)
        private val subjectNameView: AppCompatTextView = itemView.findViewById(R.id.subjectNameView)
        private val taskNameView: AppCompatTextView = itemView.findViewById(R.id.taskNameView)
        private val dueDateView: AppCompatTextView = itemView.findViewById(R.id.dueDateView)
        private val attachmentView: AppCompatTextView = itemView.findViewById(R.id.attachmentsView)
        private val tagView: View = itemView.findViewById(R.id.tagView)

        private fun formatDate(core: Core) {
            val currentTime = LocalDateTime.now()
            if (core.task.dueDate?.isBefore(currentTime) == true && !core.task.isFinished) {
                dueDateView.setTextColor(ContextCompat.getColor(rootView.context,
                    R.color.colorSwipeLeft))
                dueDateView.text = String.format(rootView.context.getString(R.string.missed),
                    Task.formatDueDate(itemView.context, core.task.dueDate!!))
            } else {
                dueDateView.text = Task.formatDueDate(itemView.context, core.task.dueDate!!)
                dueDateView.setTextColor(ContextCompat.getColor(rootView.context,
                    R.color.colorOnSurface))
            }
        }

        fun onBind(core: Core) {
            subjectNameView.text = core.subject.description ?: core.subject.code
            tagView.setBackgroundColor(core.subject.tag.actualColor)

            checkBox.isChecked = core.task.isFinished
            taskNameView.text = core.task.name
            formatDate(core)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                core.task.isFinished = isChecked
                if (isChecked) {
                    taskNameView.paintFlags = taskNameView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    formatDate(core)
                } else
                    taskNameView.paintFlags = taskNameView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                actionListener.onActionPerformed(core, ActionListener.Action.MODIFY)
            }

            if (core.task.isFinished)
                taskNameView.paintFlags = taskNameView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            attachmentView.isVisible = core.attachmentList.isNotEmpty()
            attachmentView.text = itemView.context.resources.getQuantityString(R.plurals.files_attached,
                core.attachmentList.size, core.attachmentList.size)

            rootView.setOnClickListener {
                actionListener.onActionPerformed(core, ActionListener.Action.SELECT)
            }
        }
    }
}