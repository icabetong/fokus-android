package com.isaiahvonrundstedt.fokus.features.event.previous

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.features.event.EventAdapter
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.event.EventViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import kotlinx.android.synthetic.main.activity_previous.*
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.layout_empty_previous.*

class PreviousEventsActivity: BaseActivity(), BaseListAdapter.ActionListener {

    private val adapter = EventAdapter(this)

    private val viewModel by lazy {
        ViewModelProvider(this).get(EventViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previous)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_previous)

        recyclerView.addItemDecoration(ItemDecoration(this))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.previousEvents.observe(this) { adapter.submitList(it) }
        viewModel.noPreviousEvents.observe(this) { emptyView.isVisible = it }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is EventPackage) {
            when (action) {
                BaseListAdapter.ActionListener.Action.SELECT -> {}
                // Item has been swiped, notify database for deletion
                BaseListAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.event)

                    createSnackbar(R.string.feedback_event_removed, recyclerView).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.event) }
                    }
                }
            }
        }
    }

}