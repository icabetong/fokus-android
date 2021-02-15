package com.isaiahvonrundstedt.fokus.features.subject

import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.databinding.FragmentSubjectBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.viewer.ScheduleViewerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import dagger.hilt.android.AndroidEntryPoint
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.overrideOverflowMenu

@AndroidEntryPoint
class SubjectFragment : BaseFragment(), BaseAdapter.ActionListener, SubjectAdapter.ScheduleListener,
    BaseAdapter.ArchiveListener {

    private var _binding: FragmentSubjectBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val subjectAdapter = SubjectAdapter(this, this, this)
    private val viewModel: SubjectViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSubjectBinding.inflate(inflater)
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
            adapter = subjectAdapter
        }

        postponeEnterTransition()
        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }

        ItemTouchHelper(ItemSwipeCallback(requireContext(), subjectAdapter))
            .attachToRecyclerView(binding.recyclerView)

        subjectAdapter.constraint = viewModel.constraint
        viewModel.subjects.observe(viewLifecycleOwner) {
            subjectAdapter.submitList(it)
        }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            when(viewModel.constraint) {
                SubjectViewModel.Constraint.ALL -> {
                    binding.emptyViewSubjectsAll.isVisible = it
                    binding.emptyViewSubjectsToday.isVisible = false
                    binding.emptyViewSubjectsTomorrow.isVisible = false
                }
                SubjectViewModel.Constraint.TODAY -> {
                    binding.emptyViewSubjectsAll.isVisible = false
                    binding.emptyViewSubjectsToday.isVisible = it
                    binding.emptyViewSubjectsTomorrow.isVisible = false
                }
                SubjectViewModel.Constraint.TOMORROW -> {
                    binding.emptyViewSubjectsAll.isVisible = false
                    binding.emptyViewSubjectsToday.isVisible = false
                    binding.emptyViewSubjectsTomorrow.isVisible = it
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        controller = Navigation.findNavController(requireActivity(), R.id.navigationHostFragment)
        setupNavigation(binding.appBarLayout.toolbar, controller)
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            controller?.navigate(R.id.action_to_navigation_editor_subject, null, null,
                FragmentNavigatorExtras(it to TRANSITION_ELEMENT_ROOT))
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       container: View?) {
        if (t is SubjectPackage) {
            when (action) {
                // Create the intent for the editorUI and pass the extras
                // and wait for the result
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val transitionName = TRANSITION_ELEMENT_ROOT + t.subject.subjectID

                    val args = bundleOf(
                        SubjectEditor.EXTRA_SUBJECT to Subject.toBundle(t.subject),
                        SubjectEditor.EXTRA_SCHEDULE to t.schedules
                    )

                    container?.also {
                        controller?.navigate(R.id.action_to_navigation_editor_subject, args, null,
                            FragmentNavigatorExtras(it to transitionName))
                    }
                }
                // Item has been swiped from the RecyclerView, notify user action
                // in the ViewModel to delete it from the database
                // then show a SnackBar feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.subject)

                    createSnackbar(R.string.feedback_subject_removed, binding.recyclerView).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.subject, t.schedules) }
                    }
                }
            }
        }
    }

    override fun <T> onItemArchive(t: T) {
        if (t is SubjectPackage) {
            t.subject.isSubjectArchived = true
            viewModel.update(t.subject)
        }
    }

    private fun buildOptionsMenu(menu: Menu) {
        menu.addSubMenu(R.string.menu_sort).also {
            it.setIcon(R.drawable.ic_hero_sort_ascending_24)

            it.addSubMenu(R.string.field_subject_code).apply {
                setIcon(R.drawable.ic_hero_hashtag_24)

                add(R.string.sorting_directions_ascending).apply {
                    setIcon(R.drawable.ic_hero_sort_ascending_24)
                    setOnMenuItemClickListener {
                        viewModel.sort = SubjectViewModel.Sort.CODE
                        viewModel.direction = SortDirection.ASCENDING
                        true
                    }
                }
                add(R.string.sorting_directions_descending).apply {
                    setIcon(R.drawable.ic_hero_sort_descending_24)
                    setOnMenuItemClickListener {
                        viewModel.sort = SubjectViewModel.Sort.CODE
                        viewModel.direction = SortDirection.DESCENDING
                        true
                    }
                }
            }

            it.addSubMenu(R.string.field_description).apply {
                setIcon(R.drawable.ic_hero_pencil_24)

                add(R.string.sorting_directions_ascending).apply {
                    setIcon(R.drawable.ic_hero_sort_ascending_24)
                    setOnMenuItemClickListener {
                        viewModel.sort = SubjectViewModel.Sort.DESCRIPTION
                        viewModel.direction = SortDirection.ASCENDING
                        true
                    }
                }
                add(R.string.sorting_directions_descending).apply {
                    setIcon(R.drawable.ic_hero_sort_descending_24)
                    setOnMenuItemClickListener {
                        viewModel.sort = SubjectViewModel.Sort.DESCRIPTION
                        viewModel.direction = SortDirection.DESCENDING
                        true
                    }
                }
            }

            if (viewModel.constraint != SubjectViewModel.Constraint.ALL) {
                it.addSubMenu(R.string.field_schedule).apply {
                    setIcon(R.drawable.ic_hero_clock_24)

                    add(R.string.sorting_directions_ascending).apply {
                        setIcon(R.drawable.ic_hero_sort_ascending_24)
                        setOnMenuItemClickListener {
                            viewModel.sort = SubjectViewModel.Sort.SCHEDULE
                            viewModel.direction = SortDirection.ASCENDING
                            true
                        }
                    }
                    add(R.string.sorting_directions_descending).apply {
                        setIcon(R.drawable.ic_hero_sort_descending_24)
                        setOnMenuItemClickListener {
                            viewModel.sort = SubjectViewModel.Sort.SCHEDULE
                            viewModel.direction = SortDirection.DESCENDING
                            true
                        }
                    }
                }
            }
        }
        menu.addSubMenu(R.string.menu_filter).also {
            it.setIcon(R.drawable.ic_hero_filter_24)
            it.add(R.string.filter_options_all).apply {
                setIcon(R.drawable.ic_hero_clipboard_list_24)
                setOnMenuItemClickListener {
                    viewModel.constraint = SubjectViewModel.Constraint.ALL
                    subjectAdapter.constraint = viewModel.constraint
                    binding.appBarLayout.toolbar.setTitle(getToolbarTitle())
                    true
                }
            }
            it.add(R.string.filter_options_today_classes).apply {
                setIcon(R.drawable.ic_hero_exclamation_circle_24)
                setOnMenuItemClickListener {
                    viewModel.constraint = SubjectViewModel.Constraint.TODAY
                    subjectAdapter.constraint = viewModel.constraint
                    binding.appBarLayout.toolbar.setTitle(getToolbarTitle())
                    true
                }
            }
            it.add(R.string.filter_options_tomorrow_classes).apply {
                setIcon(R.drawable.ic_hero_calendar_24)
                setOnMenuItemClickListener {
                    viewModel.constraint = SubjectViewModel.Constraint.TOMORROW
                    subjectAdapter.constraint = viewModel.constraint
                    binding.appBarLayout.toolbar.setTitle(getToolbarTitle())
                    true
                }
            }
        }
    }

    override fun onScheduleListener(items: List<Schedule>) {
        ScheduleViewerSheet(items, childFragmentManager)
            .show()
    }

    @StringRes
    private fun getToolbarTitle(): Int {
        return when(viewModel.constraint) {
            SubjectViewModel.Constraint.ALL -> R.string.activity_subjects
            SubjectViewModel.Constraint.TODAY -> R.string.activity_subjects_today
            SubjectViewModel.Constraint.TOMORROW -> R.string.activity_subjects_tomorrow
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}