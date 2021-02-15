package com.isaiahvonrundstedt.fokus.features.task

import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.databinding.FragmentTaskBinding
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.editor.TaskEditor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_task.*
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.overrideAllPopupMenus
import me.saket.cascade.overrideOverflowMenu
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.io.File

@AndroidEntryPoint
class TaskFragment : BaseFragment(), BaseAdapter.ActionListener, TaskAdapter.TaskStatusListener,
    BaseAdapter.ArchiveListener {
    private var _binding: FragmentTaskBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val taskAdapter = TaskAdapter(this, this, this)
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = getTransition()
        sharedElementReturnTransition = getTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentTaskBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actionButton.transitionName = TRANSITION_ELEMENT_ROOT

        with(binding.appBarLayout.toolbar) {
            setTitle(getToolbarTitle())
            buildOptionsMenu(menu)
            overrideOverflowMenu { context, anchor -> CascadePopupMenu(context, anchor) }
        }

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        ItemTouchHelper(ItemSwipeCallback(requireContext(), taskAdapter))
            .attachToRecyclerView(binding.recyclerView)

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
            binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }
        }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            when (viewModel.filterOption) {
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

    override fun onStart() {
        super.onStart()

        controller = Navigation.findNavController(requireActivity(), R.id.navigationHostFragment)
        setupNavigation(binding.appBarLayout.toolbar, controller)

        setFragmentResultListener(TaskEditor.REQUEST_KEY_INSERT) { _, args ->
            args.getBundle(TaskEditor.EXTRA_TASK)?.also {
                Task.fromBundle(it)?.also { task ->
                    val attachments = args.getParcelableArrayList<Attachment>(TaskEditor.EXTRA_ATTACHMENTS)

                    viewModel.insert(task, attachments ?: emptyList())
                }
            }
        }
        setFragmentResultListener(TaskEditor.REQUEST_KEY_UPDATE) { _, args ->
            args.getBundle(TaskEditor.EXTRA_TASK)?.also {
                Task.fromBundle(it)?.also { task ->
                    val attachments = args.getParcelableArrayList<Attachment>(TaskEditor.EXTRA_ATTACHMENTS)

                    viewModel.insert(task, attachments ?: emptyList())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            controller?.navigate(R.id.action_to_navigation_editor_task, null, null,
                FragmentNavigatorExtras(it to TRANSITION_ELEMENT_ROOT))
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
                    val transitionName = TRANSITION_ELEMENT_ROOT + t.task.taskID

                    val args = bundleOf(
                        TaskEditor.EXTRA_TASK to Task.toBundle(t.task),
                        TaskEditor.EXTRA_ATTACHMENTS to t.attachments,
                        TaskEditor.EXTRA_SUBJECT to t.subject?.let { Subject.toBundle(it) }
                    )

                    container?.also {
                        controller?.navigate(R.id.action_to_navigation_editor_task, args, null,
                            FragmentNavigatorExtras(it to transitionName))
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

    override fun <T> onItemArchive(t: T) {
        if (t is TaskPackage) {
            t.task.isTaskArchived = true
            viewModel.update(t.task)
        }
    }

    private fun buildOptionsMenu(menu: Menu) {
        menu.addSubMenu(R.string.menu_sort).also {
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
        menu.addSubMenu(R.string.menu_filter)?.also {
            it.setIcon(R.drawable.ic_hero_filter_24)

            it.add(R.string.filter_options_all).apply {
                setIcon(R.drawable.ic_hero_clipboard_list_24)

                setOnMenuItemClickListener {
                    viewModel.filterOption = TaskViewModel.Constraint.ALL
                    binding.appBarLayout.toolbar.setTitle(getToolbarTitle())

                    true
                }
            }
            it.add(R.string.filter_options_pending_tasks).apply {
                setIcon(R.drawable.ic_hero_exclamation_circle_24)

                setOnMenuItemClickListener {
                    viewModel.filterOption = TaskViewModel.Constraint.PENDING
                    binding.appBarLayout.toolbar.setTitle(getToolbarTitle())

                    true
                }
            }
            it.add(R.string.filter_options_finished_tasks).apply {
                setIcon(R.drawable.ic_hero_check_24)

                setOnMenuItemClickListener {
                    viewModel.filterOption = TaskViewModel.Constraint.FINISHED
                    binding.appBarLayout.toolbar.setTitle(getToolbarTitle())

                    true
                }
            }
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
