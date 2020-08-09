package com.isaiahvonrundstedt.fokus.features.task

import android.app.Activity
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
import androidx.preference.Preference
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import kotlinx.android.synthetic.main.fragment_task.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class TaskFragment : BaseFragment(), BaseListAdapter.ActionListener, TaskAdapter.TaskCompletionListener {

    private val viewModel: TaskViewModel by lazy {
        ViewModelProvider(this).get(TaskViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TaskAdapter(this, this)
        recyclerView.addItemDecoration(ItemDecoration(requireContext(),
            R.dimen.item_offset_vertical, R.dimen.item_offset_horizontal))
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

    // Update the task in the database then show
    // snackbar feedback and also if the sounds if turned on
    // play a fokus sound.
    override fun <T> onTaskCompleted(t: T, isChecked: Boolean) {
        if (t is TaskResource) {
            viewModel.update(t.task)
            createSnackbar(recyclerView, R.string.button_mark_as_finished).show()

            with(PreferenceManager(context)) {
                if (confettiEnabled) {
                    confettiView.build()
                        .addColors(Color.YELLOW, Color.MAGENTA, Color.CYAN,
                            Color.GREEN, Color.RED, Color.BLUE)
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

                if (soundEnabled) {
                    var soundUri: Uri = PreferenceManager.DEFAULT_SOUND_URI
                    if (customSoundEnabled)
                       soundUri = this.customSoundUri

                    RingtoneManager.getRingtone(requireContext(), soundUri).play()
                }
            }
        }
    }

    // Callback from the RecyclerView Adapter
    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is TaskResource) {
            when (action) {
                // Create the intent to the editorUI and pass the extras
                // and wait for the result.
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, TaskEditor::class.java).apply {
                        putExtra(TaskEditor.EXTRA_TASK, t.task)
                        putExtra(TaskEditor.EXTRA_SUBJECT, t.subject)
                        putExtra(TaskEditor.EXTRA_ATTACHMENTS, t.attachments.toArrayList())
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
                            viewModel.insert(t.task, t.attachments)
                        }
                        show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == TaskEditor.REQUEST_CODE_INSERT
            || requestCode == TaskEditor.REQUEST_CODE_UPDATE) {

            val task: Task? = data?.getParcelableExtra(TaskEditor.EXTRA_TASK)
            val attachments: List<Attachment>? = data?.getParcelableListExtra(TaskEditor.EXTRA_ATTACHMENTS)

            task?.also {
                when (requestCode) {
                    TaskEditor.REQUEST_CODE_INSERT ->
                        viewModel.insert(it, attachments ?: emptyList())
                    TaskEditor.REQUEST_CODE_UPDATE ->
                        viewModel.update(it, attachments ?: emptyList())
                }
            }
        }
    }
}
