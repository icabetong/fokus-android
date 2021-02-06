package com.isaiahvonrundstedt.fokus.features.task

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toArrayList
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.databinding.FragmentTaskBinding
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.task.editor.TaskEditor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_task.*
import me.saket.cascade.CascadePopupMenu
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.io.File

@AndroidEntryPoint
class TaskFragment : BaseFragment(), BaseAdapter.ActionListener, TaskAdapter.TaskStatusListener,
    BaseAdapter.ArchiveListener {

    private var _binding: FragmentTaskBinding? = null

    private val binding get() = _binding!!
    private val taskAdapter = TaskAdapter(this, this, this)
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentTaskBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityToolbar?.setTitle(getToolbarTitle())

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }

        ItemTouchHelper(ItemSwipeCallback(requireContext(), taskAdapter))
            .attachToRecyclerView(binding.recyclerView)

        viewModel.tasks.observe(viewLifecycleOwner) { taskAdapter.submitList(it) }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            when(viewModel.filterOption) {
                TaskViewModel.Constraint.ALL -> {
                    binding.emptyViewPendingTasks.isVisible = it
                    binding.emptyViewFinishedTasks.isVisible = false
                }
                TaskViewModel.Constraint.PENDING -> {
                    binding.emptyViewPendingTasks.isVisible = it
                    binding.emptyViewFinishedTasks.isVisible = false
                }
                TaskViewModel.Constraint.FINISHED -> {
                    binding.emptyViewPendingTasks.isVisible = false
                    binding.emptyViewFinishedTasks.isVisible = it
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            startActivityForResult(Intent(context, TaskEditor::class.java),
                TaskEditor.REQUEST_CODE_INSERT, buildTransitionOptions(it))
        }
    }

    // Update the task in the database then show
    // snackbar feedback and also if the sounds if turned on
    // play a fokus sound.
    override fun onStatusChanged(taskPackage: TaskPackage, isFinished: Boolean) {
        viewModel.update(taskPackage.task)
        if (isFinished) {
            createSnackbar(R.string.feedback_task_marked_as_finished, binding.recyclerView)

            with(PreferenceManager(context)) {
                if (confetti) {
                    binding.confettiView.build()
                        .addColors(Color.YELLOW, Color.MAGENTA, Color.CYAN)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(1000L)
                        .addShapes(Shape.Square, Shape.Circle)
                        .addSizes(Size(12, 5f))
                        .setPosition(binding.confettiView.x + binding.confettiView.width / 2,
                            binding.confettiView.y + binding.confettiView.height / 3)
                        .burst(100)
                }

                if (sounds)
                    RingtoneManager.getRingtone(requireContext(),
                        Uri.parse(PreferenceManager.DEFAULT_SOUND)).play()
            }
        }
    }

    // Callback from the RecyclerView Adapter
    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       container: View?) {
        if (t is TaskPackage) {
            when (action) {
                // Create the intent to the editorUI and pass the extras
                // and wait for the result.
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, TaskEditor::class.java).apply {
                        putExtra(TaskEditor.EXTRA_TASK, t.task)
                        putExtra(TaskEditor.EXTRA_SUBJECT, t.subject)
                        putExtra(TaskEditor.EXTRA_ATTACHMENTS, t.attachments.toArrayList())
                    }

                    container?.also {
                        startActivityForResult(intent, TaskEditor.REQUEST_CODE_UPDATE,
                            buildTransitionOptions(it, it.transitionName))
                    }
                }
                // The item has been swiped down from the recyclerView
                // remove the item from the database and show a snackbar
                // feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.task)

                    createSnackbar(R.string.feedback_task_removed, recyclerView).run {
                        addCallback(object: Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)

                                if (event != DISMISS_EVENT_ACTION)
                                    t.attachments.forEach { attachment ->
                                        if (attachment.type == Attachment.TYPE_IMPORTED_FILE)
                                            attachment.target?.also { File(it).delete() }
                                    }
                            }
                        })
                        setAction(R.string.button_undo) {
                            viewModel.insert(t.task, t.attachments)
                        }
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

    override fun <T> onItemArchive(t: T) {
        if (t is TaskPackage) {
            t.task.isTaskArchived = true
            viewModel.update(t.task)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_more -> {
                activityToolbar?.findViewById<View?>(R.id.action_more)?.also { view ->
                    val optionsMenu = CascadePopupMenu(requireContext(), view)
                    optionsMenu.menu.addSubMenu(R.string.menu_sort).also {
                        it.setIcon(R.drawable.ic_hero_sort_ascending_24)

                        it.addSubMenu(R.string.field_task_name)?.apply {
                            setIcon(R.drawable.ic_hero_pencil_24)

                            add(R.string.sorting_directions_ascending).apply {
                                setIcon(R.drawable.ic_hero_sort_ascending_24)

                                setOnMenuItemClickListener {
                                    viewModel.sort = TaskViewModel.Sort.NAME
                                    viewModel.sortDirection = SortDirection.ASCENDING

                                    true
                                }
                            }
                            add(R.string.sorting_directions_descending).apply {
                                setIcon(R.drawable.ic_hero_sort_descending_24)

                                setOnMenuItemClickListener {
                                    viewModel.sort = TaskViewModel.Sort.NAME
                                    viewModel.sortDirection = SortDirection.DESCENDING

                                    true
                                }
                            }
                        }
                        it.addSubMenu(R.string.field_due_date).apply {
                            setIcon(R.drawable.ic_hero_clock_24)

                            add(R.string.sorting_directions_ascending).apply {
                                setIcon(R.drawable.ic_hero_sort_ascending_24)
                                setOnMenuItemClickListener {
                                    viewModel.sort = TaskViewModel.Sort.DUE
                                    viewModel.sortDirection = SortDirection.ASCENDING

                                    true
                                }
                            }
                            add(R.string.sorting_directions_descending).apply {
                                setIcon(R.drawable.ic_hero_sort_descending_24)
                                setOnMenuItemClickListener {
                                    viewModel.sort = TaskViewModel.Sort.DUE
                                    viewModel.sortDirection = SortDirection.DESCENDING

                                    true
                                }
                            }
                        }
                    }
                    optionsMenu.menu.addSubMenu(R.string.menu_filter).also {
                        it.setIcon(R.drawable.ic_hero_filter_24)

                        it.add(R.string.filter_options_all).apply {
                            setIcon(R.drawable.ic_hero_clipboard_list_24)

                            setOnMenuItemClickListener {
                                viewModel.filterOption = TaskViewModel.Constraint.ALL
                                activityToolbar?.setTitle(getToolbarTitle())

                                true
                            }
                        }
                        it.add(R.string.filter_options_pending_tasks).apply {
                            setIcon(R.drawable.ic_hero_exclamation_circle_24)

                            setOnMenuItemClickListener {
                                viewModel.filterOption = TaskViewModel.Constraint.PENDING
                                activityToolbar?.setTitle(getToolbarTitle())

                                true
                            }
                        }
                        it.add(R.string.filter_options_finished_tasks).apply {
                            setIcon(R.drawable.ic_hero_check_24)

                            setOnMenuItemClickListener {
                                viewModel.filterOption = TaskViewModel.Constraint.FINISHED
                                activityToolbar?.setTitle(getToolbarTitle())

                                true
                            }
                        }
                    }
                    optionsMenu.show()
                }
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @StringRes
    private fun getToolbarTitle(): Int {
        return when (viewModel.filterOption) {
            TaskViewModel.Constraint.ALL -> R.string.activity_tasks
            TaskViewModel.Constraint.PENDING -> R.string.activity_tasks_pending
            TaskViewModel.Constraint.FINISHED -> R.string.activity_tasks_finished
        }
    }
}
