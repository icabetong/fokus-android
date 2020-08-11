package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
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
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import kotlinx.android.synthetic.main.fragment_subject.*

class SubjectFragment : BaseFragment(), BaseListAdapter.ActionListener {

    private val viewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubjectAdapter(this)
        recyclerView.addItemDecoration(ItemDecoration(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.fetch()?.observe(viewLifecycleOwner, Observer { items ->
            adapter?.submitList(items)
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private var adapter: SubjectAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, SubjectEditor::class.java),
                SubjectEditor.REQUEST_CODE_INSERT)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is SubjectResource) {
            when (action) {
                // Create the intent for the editorUI and pass the extras
                // and wait for the result
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, SubjectEditor::class.java).apply {
                        putExtra(SubjectEditor.EXTRA_SUBJECT, t.subject)
                        putExtra(SubjectEditor.EXTRA_SCHEDULE, t.schedules)
                    }
                    startActivityWithTransition(views, intent, SubjectEditor.REQUEST_CODE_UPDATE)
                }
                // Item has been swiped from the RecyclerView, notify user action
                // in the ViewModel to delete it from the database
                // then show a SnackBar feedback
                BaseListAdapter.ActionListener.Action.DELETE -> {
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

}