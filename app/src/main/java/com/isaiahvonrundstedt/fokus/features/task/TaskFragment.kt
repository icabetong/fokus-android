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
import com.isaiahvonrundstedt.fokus.features.core.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
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
            startActivityForResult(Intent(context, TaskEditor::class.java),
                TaskEditor.insertRequestCode)
        }
    }

    // Callback from the RecyclerView Adapter
    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is TaskResource) {
            when (action) {
                // Update the task in the database then show
                // snackbar feedback and also if the sounds if turned on
                // play a fokus sound. Primarily, MODIFY is used when
                // the checkbox is checked, indicating that the
                // task has been marked as finished.
                BaseAdapter.ActionListener.Action.MODIFY -> {
                    viewModel.update(t.task)
                    if (t.task.isFinished) {
                        createSnackbar(recyclerView, R.string.feedback_task_marked_as_finished).show()
                        if (PreferenceManager(context).soundEnabled) {
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
                    startActivityWithTransition(views, intent, TaskEditor.updateRequestCode)
                }
                // The item has been swiped down from the recyclerView
                // remove the item from the database and show a snackbar
                // feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.task)

                     createSnackbar(recyclerView, R.string.feedback_task_removed).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.task, t.attachmentList) }
                        show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == TaskEditor.insertRequestCode
                || requestCode == TaskEditor.updateRequestCode) {

            if (resultCode == Activity.RESULT_OK) {
                val task: Task? = data?.getParcelableExtra(TaskEditor.extraTask)
                val attachments: List<Attachment>? = data?.getParcelableArrayListExtra(TaskEditor.extraAttachments)

                task?.let {
                    if (requestCode == TaskEditor.insertRequestCode)
                        viewModel.insert(it, attachments ?: emptyList())
                    else viewModel.update(it, attachments ?: emptyList())
                }
            }
        }
    }
}
