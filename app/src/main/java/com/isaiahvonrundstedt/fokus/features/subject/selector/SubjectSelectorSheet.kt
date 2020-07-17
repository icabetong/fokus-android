package com.isaiahvonrundstedt.fokus.features.subject.selector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import kotlinx.android.synthetic.main.fragment_subject.*
import kotlinx.android.synthetic.main.layout_appbar_selector.*

class SubjectSelectorSheet(fragmentManager: FragmentManager)
    : BaseBottomSheet<Subject>(fragmentManager), BaseListAdapter.ActionListener {

    private val viewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SubjectSelectorAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.fetch()?.observe(this, Observer {
            adapter.submitList(it)
            emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SubjectEditor.REQUEST_CODE_INSERT && resultCode == Activity.RESULT_OK) {
            val subject: Subject?
                    = data?.getParcelableExtra(SubjectEditor.EXTRA_SUBJECT)
            val scheduleList: List<Schedule>?
                    = data?.getParcelableListExtra(SubjectEditor.EXTRA_SCHEDULE)

            subject?.let {
                viewModel.insert(it, scheduleList ?: emptyList())
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Subject) {
            when (action) {
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    callback?.invoke(t)
                    this.dismiss()
                }
                BaseListAdapter.ActionListener.Action.MODIFY -> { }
                BaseListAdapter.ActionListener.Action.DELETE -> { }
            }
        }
    }
}