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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.core.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.components.sheet.FirstRunBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
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

        if (PreferenceManager(context).isFirstRun)
            FirstRunBottomSheet().invoke(childFragmentManager)

        adapter = TaskAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
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
            startEditorWithTransition(it, Intent(context, TaskEditor::class.java),
                TaskEditor.insertRequestCode)
        }
    }

    // Callback from the RecyclerView Adapter
    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       itemView: View) {
        if (t is Core) {
            when (action) {
                // Update the task in the database then show
                // snackbar feedback and also if the sounds if turned on
                // play a fokus sound. Primarily, MODIFY is used when
                // the checkbox is checked, indicating that the
                // task has been marked as finished.
                BaseAdapter.ActionListener.Action.MODIFY -> {
                    viewModel.update(t.task)
                    if (t.task.isFinished) {
                        if (PreferenceManager(context).soundEnabled) {
                            Snackbar.make(recyclerView, R.string.feedback_task_marked_as_finished,
                                Snackbar.LENGTH_SHORT).show()
                            val uri: Uri = PreferenceManager(requireContext()).let {
                                if (it.customSoundEnabled)
                                    it.soundUri
                                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            }
                            RingtoneManager.getRingtone(requireContext().applicationContext, uri).play()
                        }
                    }
                }
                // Create the intent to the editorUI and pass the extras
                // and wait for the result.
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, TaskEditor::class.java).apply {
                        putExtra(TaskEditor.extraTask, t.task)
                        putExtra(TaskEditor.extraSubject, t.subject)
                        putExtra(TaskEditor.extraAttachments, t.attachmentList.toArrayList())
                    }
                    startEditorWithTransition(itemView, intent,
                        TaskEditor.updateRequestCode)
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
            val task: Task = data?.getParcelableExtra(TaskEditor.extraTask)!!
            val attachments: List<Attachment> = data.getParcelableArrayListExtra(TaskEditor.extraAttachments)!!

            // Perform an action on the database based on the
            // requestCode
            if (requestCode == TaskEditor.insertRequestCode) {
                viewModel.insert(task, attachments)
            } else if (requestCode == TaskEditor.updateRequestCode) {
                viewModel.update(task, attachments)
            }
        }
    }
}
