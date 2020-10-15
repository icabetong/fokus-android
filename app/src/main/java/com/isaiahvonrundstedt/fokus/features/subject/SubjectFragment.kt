package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.view.isVisible
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
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.viewer.ScheduleViewerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import kotlinx.android.synthetic.main.fragment_subject.*
import me.saket.cascade.CascadePopupMenu

class SubjectFragment : BaseFragment(), BaseAdapter.ActionListener, SubjectAdapter.ScheduleListener {

    private val adapter = SubjectAdapter(this, this)
    private val viewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityToolbar?.setTitle(getToolbarTitle())

        recyclerView.addItemDecoration(ItemDecoration(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.subjects.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            when(viewModel.constraint) {
                SubjectViewModel.Constraint.ALL -> {
                    emptyViewSubjectsAll.isVisible = it
                    emptyViewSubjectsToday.isVisible = false
                    emptyViewSubjectsTomorrow.isVisible = false
                }
                SubjectViewModel.Constraint.TODAY -> {
                    emptyViewSubjectsAll.isVisible = false
                    emptyViewSubjectsToday.isVisible = it
                    emptyViewSubjectsTomorrow.isVisible = false
                }
                SubjectViewModel.Constraint.TOMORROW -> {
                    emptyViewSubjectsAll.isVisible = false
                    emptyViewSubjectsToday.isVisible = false
                    emptyViewSubjectsTomorrow.isVisible = it
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, SubjectEditor::class.java),
                SubjectEditor.REQUEST_CODE_INSERT)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is SubjectPackage) {
            when (action) {
                // Create the intent for the editorUI and pass the extras
                // and wait for the result
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, SubjectEditor::class.java).apply {
                        putExtra(SubjectEditor.EXTRA_SUBJECT, t.subject)
                        putExtra(SubjectEditor.EXTRA_SCHEDULE, t.schedules)
                    }
                    startActivityWithTransition(views, intent, SubjectEditor.REQUEST_CODE_UPDATE)
                }
                // Item has been swiped from the RecyclerView, notify user action
                // in the ViewModel to delete it from the database
                // then show a SnackBar feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.subject)

                    createSnackbar(R.string.feedback_subject_removed, recyclerView).run {
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
                    optionsMenu.menu.addSubMenu(R.string.menu_filter).also {
                        it.setIcon(R.drawable.ic_hero_filter_24)
                        it.add(R.string.filter_options_all).apply {
                            setIcon(R.drawable.ic_hero_clipboard_list_24)
                            setOnMenuItemClickListener {
                                viewModel.constraint = SubjectViewModel.Constraint.ALL
                                adapter.constraint = viewModel.constraint
                                activityToolbar?.setTitle(getToolbarTitle())
                                true
                            }
                        }
                        it.add(R.string.filter_options_today_classes).apply {
                            setIcon(R.drawable.ic_hero_exclamation_circle_24)
                            setOnMenuItemClickListener {
                                viewModel.constraint = SubjectViewModel.Constraint.TODAY
                                adapter.constraint = viewModel.constraint
                                activityToolbar?.setTitle(getToolbarTitle())
                                true
                            }
                        }
                        it.add(R.string.filter_options_tomorrow_classes).apply {
                            setIcon(R.drawable.ic_hero_calendar_24)
                            setOnMenuItemClickListener {
                                viewModel.constraint = SubjectViewModel.Constraint.TOMORROW
                                adapter.constraint = viewModel.constraint
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
}