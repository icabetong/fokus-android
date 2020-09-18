package com.isaiahvonrundstedt.fokus.features.task.editor

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.createToast
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.task.Task
import kotlinx.android.synthetic.main.layout_sheet_task.*
import org.joda.time.LocalDateTime
import java.util.*

class TaskEditorSheet(fragmentManager: FragmentManager): BaseBottomSheet<Task>(fragmentManager) {

    private val task = Task()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dueDateChip.setOnClickListener { chip ->
            MaterialDialog(requireContext()).show {
                lifecycleOwner(this@TaskEditorSheet)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = task.dueDate?.toCalendar(Locale.getDefault())) { _, datetime ->
                    task.dueDate = LocalDateTime.fromCalendarFields(datetime).toDateTime()
                }
                positiveButton(R.string.button_done) {
                    if (chip is Chip) {
                        chip.text = task.formatDueDate(context)
                        chip.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        expandButton.setOnClickListener {
            taskNameTextInput.transitionName = TaskEditor.TRANSITION_ID_NAME

            task.name = taskNameTextInput.text.toString()
            val editor = Intent(context, TaskEditor::class.java)
            editor.putExtra(EXTRA_TASK_IS_EXPANDED, true)
            editor.putExtra(EXTRA_TASK_TITLE, task.name)

            if (task.hasDueDate())
                editor.putExtra(EXTRA_TASK_DUE, DateTimeConverter.fromDateTime(task.dueDate))

            val sharedViews = mapOf<String, View>(TaskEditor.TRANSITION_ID_NAME
                    to taskNameTextInput).mapNotNull {
                Pair.create(it.value, it.key)
            }.toTypedArray()

            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(),
                *sharedViews)
            activity?.startActivityForResult(editor, TaskEditor.REQUEST_CODE_INSERT,
                options.toBundle())
        }

        actionButton.setOnClickListener {
            task.name = taskNameTextInput.text.toString()

            if (task.name.isNullOrEmpty()) {
                createToast(R.string.feedback_task_empty_name)
                taskNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!task.hasDueDate()) {
                createToast(R.string.feedback_task_empty_due_date)
                dueDateChip.performClick()
                return@setOnClickListener
            }

            receiver?.onReceive(task)
        }
    }

    companion object {
        const val EXTRA_TASK_IS_EXPANDED = "extra:task:expanded"
        const val EXTRA_TASK_TITLE = "extra:task:title"
        const val EXTRA_TASK_DUE = "extra:task:due"
    }
}