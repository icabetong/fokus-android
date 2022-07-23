package com.isaiahvonrundstedt.fokus.features.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import me.saket.cascade.overrideOverflowMenu


@AndroidEntryPoint
class SubjectFragment : BaseFragment(), BaseAdapter.ActionListener, SubjectAdapter.ScheduleListener,
    BaseAdapter.ArchiveListener {

    private var _binding: FragmentSubjectBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val subjectAdapter = SubjectAdapter(this, this, this)
    private val viewModel: SubjectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.actionButton.transitionName = TRANSITION_ELEMENT_ROOT
        setInsets(binding.root, binding.appBarLayout.toolbar,
            arrayOf(
                binding.recyclerView,
                binding.emptyViewSubjectsToday,
                binding.emptyViewSubjectsAll,
                binding.emptyViewSubjectsTomorrow
            ), binding.actionButton
        )

        with(binding.appBarLayout.toolbar) {
            setTitle(getToolbarTitle())
            menu?.clear()
            inflateMenu(R.menu.menu_subjects)
            overrideOverflowMenu(::customPopupProvider)
            setOnMenuItemClickListener(::onMenuItemClicked)
            setupNavigation(this)

            menu.findItem(R.id.action_sort_schedule)
                ?.isVisible = viewModel.constraint != SubjectViewModel.Constraint.ALL
        }

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = subjectAdapter

            ItemTouchHelper(ItemSwipeCallback(context, subjectAdapter))
                .attachToRecyclerView(this)
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        /**
         * Get the NavController here so
         * that it doesn't crash when
         * the host activity is recreated.
         */
        controller = Navigation.findNavController(requireActivity(), R.id.navigationHostFragment)

        subjectAdapter.constraint = viewModel.constraint
        viewModel.subjects.observe(viewLifecycleOwner) {
            subjectAdapter.submitList(it)
        }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            when (viewModel.constraint) {
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

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            controller?.navigate(
                R.id.action_to_navigation_editor_subject, null, null,
                FragmentNavigatorExtras(it to TRANSITION_ELEMENT_ROOT)
            )
        }
    }

    override fun <T> onActionPerformed(
        t: T, action: BaseAdapter.ActionListener.Action,
        container: View?
    ) {
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
                        controller?.navigate(
                            R.id.action_to_navigation_editor_subject, args, null,
                            FragmentNavigatorExtras(it to transitionName)
                        )
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

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_code_sort_ascending -> {
                viewModel.sort = SubjectViewModel.Sort.CODE
                viewModel.direction = SortDirection.ASCENDING
            }
            R.id.action_code_sort_descending -> {
                viewModel.sort = SubjectViewModel.Sort.CODE
                viewModel.direction = SortDirection.DESCENDING
            }
            R.id.action_description_sort_ascending -> {
                viewModel.sort = SubjectViewModel.Sort.DESCRIPTION
                viewModel.direction = SortDirection.ASCENDING
            }
            R.id.action_description_sort_descending -> {
                viewModel.sort = SubjectViewModel.Sort.DESCRIPTION
                viewModel.direction = SortDirection.DESCENDING
            }
            R.id.action_schedule_sort_ascending -> {
                viewModel.sort = SubjectViewModel.Sort.SCHEDULE
                viewModel.direction = SortDirection.ASCENDING
            }
            R.id.action_schedule_sort_descending -> {
                viewModel.sort = SubjectViewModel.Sort.SCHEDULE
                viewModel.direction = SortDirection.DESCENDING
            }
            R.id.action_filter_all -> {
                viewModel.constraint = SubjectViewModel.Constraint.ALL
                subjectAdapter.constraint = viewModel.constraint

                with(binding.appBarLayout.toolbar) {
                    setTitle(getToolbarTitle())
                    menu?.findItem(R.id.action_sort_schedule)?.isVisible = false
                }
            }
            R.id.action_filter_today -> {
                viewModel.constraint = SubjectViewModel.Constraint.TODAY
                subjectAdapter.constraint = viewModel.constraint

                with(binding.appBarLayout.toolbar) {
                    setTitle(getToolbarTitle())
                    menu?.findItem(R.id.action_sort_schedule)?.isVisible = false
                }
            }
            R.id.action_filter_tomorrow -> {
                viewModel.constraint = SubjectViewModel.Constraint.TOMORROW
                subjectAdapter.constraint = viewModel.constraint
                with(binding.appBarLayout.toolbar) {
                    setTitle(getToolbarTitle())
                    menu?.findItem(R.id.action_sort_schedule)?.isVisible = true
                }
            }
            R.id.action_archived -> {
                controller?.navigate(R.id.action_to_navigation_archived_subject)
            }
        }
        return true
    }

    override fun onScheduleListener(items: List<Schedule>) {
        ScheduleViewerSheet(items, childFragmentManager)
            .show()
    }

    @StringRes
    private fun getToolbarTitle(): Int {
        return when (viewModel.constraint) {
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