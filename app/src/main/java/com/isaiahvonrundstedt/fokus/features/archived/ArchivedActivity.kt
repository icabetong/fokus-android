package com.isaiahvonrundstedt.fokus.features.archived

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.features.shared.custom.OffsetItemDecoration
import kotlinx.android.synthetic.main.activity_archived.*
import kotlinx.android.synthetic.main.layout_appbar.*

class ArchivedActivity: BaseActivity(), BaseAdapter.ActionListener, BaseAdapter.SwipeListener {

    private val viewModel: ArchivedViewModel by lazy {
        ViewModelProvider(this).get(ArchivedViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archived)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_archived)

        adapter = ArchivedAdapter(this, this)
        recyclerView.addItemDecoration(OffsetItemDecoration(this, R.dimen.item_padding))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private var adapter: ArchivedAdapter? = null
    override fun onStart() {
        super.onStart()

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Core){
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {}
                BaseAdapter.ActionListener.Action.MODIFY -> {}
            }
        }
    }

    override fun <T> onSwipePerformed(position: Int, t: T, swipeDirection: Int) {
        if (t is Core) {
            if (swipeDirection == ItemTouchHelper.START) {
                viewModel.remove(t.task)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_task_removed,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    viewModel.insert(t.task, t.attachmentList)
                }
                snackbar.show()
            } else if (swipeDirection == ItemTouchHelper.END) {
                t.task.isArchived = false
                viewModel.update(t.task, t.attachmentList)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_task_removed_archived,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    t.task.isArchived = false
                    viewModel.insert(t.task, t.attachmentList)
                }
                snackbar.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_clear, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_items -> {
                viewModel.clear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}