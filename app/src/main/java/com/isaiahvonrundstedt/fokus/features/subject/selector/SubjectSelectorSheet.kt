package com.isaiahvonrundstedt.fokus.features.subject.selector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import kotlinx.android.synthetic.main.fragment_subject.*
import kotlinx.android.synthetic.main.fragment_subject.recyclerView
import kotlinx.android.synthetic.main.layout_sheet_subject.*

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
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.fetch()?.observe(this, Observer {
            adapter.submitList(it)
            emptyView.isVisible = it.isEmpty()
        })
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Subject) {
            when (action) {
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    receiver?.onReceive(t)
                    this.dismiss()
                }
                BaseListAdapter.ActionListener.Action.DELETE -> { }
            }
        }
    }
}