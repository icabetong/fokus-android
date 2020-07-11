package com.isaiahvonrundstedt.fokus.features.task

import android.content.Intent
import android.graphics.Color
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
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import kotlinx.android.synthetic.main.fragment_task.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class TaskFragment : BaseFragment(), BaseListAdapter.ActionListener {

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
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })

    }

    private var adapter: TaskAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, TaskEditor::class.java),
                TaskEditor.REQUEST_CODE_INSERT)
        }
    }

    // Callback from the RecyclerView Adapter
    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is TaskResource) {
            when (action) {
                // Update the task in the database then show
                // snackbar feedback and also if the sounds if turned on
                // play a fokus sound. Primarily, MODIFY is used when
                // the checkbox is checked, indicating that the
                // task has been marked as finished.
                BaseListAdapter.ActionListener.Action.MODIFY -> {
                    viewModel.update(t.task)
                    if (t.task.isFinished) {
                        createSnackbar(recyclerView,
                            R.string.feedback_task_marked_as_finished).show()
                        with(PreferenceManager(context)) {
                            if (soundEnabled) {
                                val uri: Uri = this.let {
                                    if (it.customSoundEnabled)
                                        it.customSoundUri
                                    else PreferenceManager.DEFAULT_SOUND_URI
                                }
                                RingtoneManager.getRingtone(requireContext().applicationContext,
                                    uri).play()
                            }

                            if (confettiEnabled) {
                                confettiView.build()
                                    .addColors(Color.YELLOW, Color.MAGENTA, Color.CYAN)
                                    .setDirection(0.0, 359.0)
                                    .setSpeed(1f, 5f)
                                    .setFadeOutEnabled(true)
                                    .setTimeToLive(1000L)
                                    .addShapes(Shape.Square, Shape.Circle)
                                    .addSizes(Size(12, 5f))
                                    .setPosition(confettiView.x + confettiView.width / 2,
                                        confettiView.y + confettiView.height / 3)
                                    .burst(100)
                            }
                        }
                    }
                }
                // Create the intent to the editorUI and pass the extras
                // and wait for the result.
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, TaskEditor::class.java).apply {
                        putExtra(TaskEditor.EXTRA_TASK, t.task)
                        putExtra(TaskEditor.EXTRA_SUBJECT, t.subject)
                        putExtra(TaskEditor.EXTRA_ATTACHMENTS, t.attachmentList.toArrayList())
                    }
                    startActivityWithTransition(views, intent, TaskEditor.REQUEST_CODE_UPDATE)
                }
                // The item has been swiped down from the recyclerView
                // remove the item from the database and show a snackbar
                // feedback
                BaseListAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.task)

                    createSnackbar(recyclerView, R.string.feedback_task_removed).run {
                        setAction(R.string.button_undo) {
                            viewModel.insert(t.task, t.attachmentList)
                        }
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
        if (requestCode == TaskEditor.REQUEST_CODE_INSERT
            || requestCode == TaskEditor.REQUEST_CODE_UPDATE) {

            if (resultCode == BaseEditor.RESULT_OK || resultCode == BaseEditor.RESULT_DELETE) {
                val task: Task? = data?.getParcelableExtra(TaskEditor.EXTRA_TASK)
                val attachments: List<Attachment>? =
                    data?.getParcelableArrayListExtra(TaskEditor.EXTRA_ATTACHMENTS)

                task?.also {
                    if (resultCode == BaseEditor.RESULT_OK) {
                        if (requestCode == TaskEditor.REQUEST_CODE_INSERT)
                            viewModel.insert(it, attachments ?: emptyList())
                        else viewModel.update(it, attachments ?: emptyList())
                    } else {
                        viewModel.remove(it)
                        createSnackbar(recyclerView, R.string.feedback_task_removed).apply {
                            setAction(R.string.button_undo) { _ -> viewModel.insert(it) }
                            show()
                        }
                    }
                }
            }
        }
    }
}
