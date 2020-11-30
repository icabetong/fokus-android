package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.android.putExtra
import com.isaiahvonrundstedt.fokus.databinding.FragmentSubjectBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.viewer.ScheduleViewerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import dagger.hilt.android.AndroidEntryPoint
import me.saket.cascade.CascadePopupMenu

@AndroidEntryPoint
class SubjectFragment : BaseFragment(), BaseAdapter.ActionListener, SubjectAdapter.ScheduleListener {

    private var _binding: FragmentSubjectBinding? = null

    private val binding get() = _binding!!
    private val subjectAdapter = SubjectAdapter(this, this)
    private val viewModel: SubjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSubjectBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityToolbar?.setTitle(getToolbarTitle())

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = subjectAdapter
        }

        ItemTouchHelper(ItemSwipeCallback(requireContext(), subjectAdapter))
            .attachToRecyclerView(binding.recyclerView)

        subjectAdapter.constraint = viewModel.constraint
        viewModel.subjects.observe(viewLifecycleOwner) { subjectAdapter.submitList(it) }
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

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            startActivityForResult(Intent(context, SubjectEditor::class.java),
                SubjectEditor.REQUEST_CODE_INSERT, buildTransitionOptions(it))
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       container: View?) {
        if (t is SubjectPackage) {
            when (action) {
                // Create the intent for the editorUI and pass the extras
                // and wait for the result
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, SubjectEditor::class.java).apply {
                        putExtra(SubjectEditor.EXTRA_SUBJECT, t.subject)
                        putExtra(SubjectEditor.EXTRA_SCHEDULE, t.schedules)
                    }

                    container?.also {
                        startActivityForResult(intent, SubjectEditor.REQUEST_CODE_UPDATE,
                            buildTransitionOptions(it, it.transitionName))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == SubjectEditor.REQUEST_CODE_INSERT
            || requestCode == SubjectEditor.REQUEST_CODE_UPDATE) {

            val subject: Subject? = data?.getParcelableExtra(SubjectEditor.EXTRA_SUBJECT)
            val scheduleList: List<Schedule>? = data?.getParcelableListExtra(SubjectEditor.EXTRA_SCHEDULE)

            subject?.also {
                when (requestCode) {
                    SubjectEditor.REQUEST_CODE_INSERT ->
                        viewModel.insert(it, scheduleList ?: emptyList())
                    SubjectEditor.REQUEST_CODE_UPDATE ->
                        viewModel.update(it, scheduleList ?: emptyList())
                }
            }
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
                    optionsMenu.menu.addSubMenu(R.string.menu_filter).also {
                        it.setIcon(R.drawable.ic_hero_filter_24)
                        it.add(R.string.filter_options_all).apply {
                            setIcon(R.drawable.ic_hero_clipboard_list_24)
                            setOnMenuItemClickListener {
                                viewModel.constraint = SubjectViewModel.Constraint.ALL
                                subjectAdapter.constraint = viewModel.constraint
                                activityToolbar?.setTitle(getToolbarTitle())
                                true
                            }
                        }
                        it.add(R.string.filter_options_today_classes).apply {
                            setIcon(R.drawable.ic_hero_exclamation_circle_24)
                            setOnMenuItemClickListener {
                                viewModel.constraint = SubjectViewModel.Constraint.TODAY
                                subjectAdapter.constraint = viewModel.constraint
                                activityToolbar?.setTitle(getToolbarTitle())
                                true
                            }
                        }
                        it.add(R.string.filter_options_tomorrow_classes).apply {
                            setIcon(R.drawable.ic_hero_calendar_24)
                            setOnMenuItemClickListener {
                                viewModel.constraint = SubjectViewModel.Constraint.TOMORROW
                                subjectAdapter.constraint = viewModel.constraint
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