package com.isaiahvonrundstedt.fokus.features.task

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.features.shared.custom.OffsetItemDecoration
import com.isaiahvonrundstedt.fokus.features.subject.SubjectActivity
import kotlinx.android.synthetic.main.fragment_task.*

class TaskFragment: BaseFragment(), BaseAdapter.ActionListener {

    private val viewModel: TaskViewModel by lazy {
        ViewModelProvider(this).get(TaskViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PreferenceManager(context).isFirstRun) {
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(R.string.first_run_add_first_subject_title)
                message(R.string.first_run_add_first_subject_message)
                positiveButton(R.string.button_continue) {
                    startActivity(Intent(context, SubjectActivity::class.java).apply {
                        action = SubjectActivity.action
                    })
                }
            }
        }

        adapter = TaskAdapter(this)
        recyclerView.addItemDecoration(OffsetItemDecoration(requireContext(), R.dimen.item_padding))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.fetch()?.observe(viewLifecycleOwner, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })

    }

    private var adapter: TaskAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, TaskEditorActivity::class.java),
                TaskEditorActivity.insertRequestCode)
        }
    }

    // Callback from the RecyclerView Adapter
    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Core) {
            when (action) {
                // Update the task in the database then show
                // snackbar feedback and also if the sounds if turned on
                // play a notification sound. Primarily, MODIFY is used when
                // the checkbox is checked, indicating that the
                // task has been marked as finished.
                BaseAdapter.ActionListener.Action.MODIFY -> {
                    viewModel.update(t.task)
                    if (t.task.isFinished) {
                        if (PreferenceManager(context).completedSounds) {
                            Snackbar.make(recyclerView, R.string.feedback_task_marked_as_finished,
                                Snackbar.LENGTH_SHORT).show()
                            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            RingtoneManager.getRingtone(requireContext().applicationContext, soundUri).play()
                        }
                    }
                }
                // Create the intent to the editorUI and pass the extras
                // and wait for the result.
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val editorIntent = Intent(context, TaskEditorActivity::class.java)
                    editorIntent.putExtra(TaskEditorActivity.extraSubject, t.subject)
                    editorIntent.putExtra(TaskEditorActivity.extraTask, t.task)
                    editorIntent.putParcelableArrayListExtra(TaskEditorActivity.extraAttachments, ArrayList(t.attachmentList))
                    startActivityForResult(editorIntent, TaskEditorActivity.updateRequestCode)
                }
                // The item has been swiped down from the recyclerView
                // remove the item from the database and show a snackbar
                // feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.task)

                    val snackbar = Snackbar.make(recyclerView, R.string.feedback_task_removed,
                        Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.button_undo) { viewModel.insert(t.task, t.attachmentList) }
                    snackbar.show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val task: Task = data?.getParcelableExtra(TaskEditorActivity.extraTask)!!
            val attachments: List<Attachment> = data.getParcelableArrayListExtra(TaskEditorActivity.extraAttachments)!!

            // Perform an action on the database based on the
            // requestCode
            if (requestCode == TaskEditorActivity.insertRequestCode) {
                viewModel.insert(task, attachments)
            } else if (requestCode == TaskEditorActivity.updateRequestCode) {
                viewModel.update(task, attachments)
            }
        }
    }
}
