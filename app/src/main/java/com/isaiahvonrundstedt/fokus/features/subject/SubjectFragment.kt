package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.android.putExtra
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import kotlinx.android.synthetic.main.fragment_subject.*
import kotlinx.android.synthetic.main.layout_sheet_filter_subject.*

class SubjectFragment : BaseFragment(), BaseAdapter.ActionListener {

    private val adapter = SubjectAdapter(this)

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

        recyclerView.addItemDecoration(ItemDecoration(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.subjects.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            when(viewModel.filterOption) {
                SubjectViewModel.FilterOption.ALL -> {
                    emptyViewSubjectsAll.isVisible = it
                    emptyViewSubjectsToday.isVisible = false
                }
                SubjectViewModel.FilterOption.TODAY -> {
                    emptyViewSubjectsAll.isVisible = false
                    emptyViewSubjectsToday.isVisible = it
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
        inflater.inflate(R.menu.menu_filter, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                FilterOptionSheet(childFragmentManager, viewModel.filterOption).show {
                    waitForResult { option ->
                        viewModel.filterOption = option
                        adapter.currentOption = option
                        this.dismiss()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class FilterOptionSheet(manager: FragmentManager, private val option: SubjectViewModel.FilterOption)
        : BaseBottomSheet<SubjectViewModel.FilterOption>(manager) {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.layout_sheet_filter_subject, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            when(option) {
                SubjectViewModel.FilterOption.ALL ->
                    filterOptionShowAll.isChecked = true
                SubjectViewModel.FilterOption.TODAY ->
                    filterOptionShowToday.isChecked = true
            }

            filterOptionGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId) {
                    R.id.filterOptionShowAll ->
                        receiver?.onReceive(SubjectViewModel.FilterOption.ALL)
                    R.id.filterOptionShowToday ->
                        receiver?.onReceive(SubjectViewModel.FilterOption.TODAY)
                }
            }

        }
    }
}