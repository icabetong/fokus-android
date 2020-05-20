package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.features.shared.custom.OffsetItemDecoration
import kotlinx.android.synthetic.main.activity_event.*
import kotlinx.android.synthetic.main.layout_appbar.*

class EventActivity: BaseActivity(), BaseAdapter.ActionListener, BaseAdapter.SwipeListener {

    private val viewModel: EventViewModel? by lazy {
        ViewModelProvider(this).get(EventViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        setPersistentActionBar(toolbar, R.string.activity_events)

        adapter = EventAdapter(this, this)
        recyclerView.addItemDecoration(OffsetItemDecoration(this, R.dimen.item_padding))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel?.fetch()?.observe(this, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.isVisible = items.isEmpty()
        })
    }

    private var adapter: EventAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            val editorIntent = Intent(this, EventEditorActivity::class.java)
            startActivityForResult(editorIntent, EventEditorActivity.insertRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Event) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {

                }
                BaseAdapter.ActionListener.Action.MODIFY -> { }
            }
        }
    }

    override fun <T> onSwipePerformed(position: Int, t: T, swipeDirection: Int) {
        if (t is Event) {
            if (swipeDirection == ItemTouchHelper.START) {
                viewModel?.remove(t)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_event_removed,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    viewModel?.insert(t)
                }
                snackbar.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val event: Event = data?.getParcelableExtra(EventEditorActivity.extraEvent)!!

            if (requestCode == EventEditorActivity.insertRequestCode) {
                viewModel?.insert(event)
            } else if (requestCode == EventEditorActivity.updateRequestCode) {
                viewModel?.update(event)
            }
        }
    }
}