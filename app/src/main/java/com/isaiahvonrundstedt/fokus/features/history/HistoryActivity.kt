package com.isaiahvonrundstedt.fokus.features.history

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.layout_appbar.*

class HistoryActivity : BaseActivity(), BaseListAdapter.ActionListener {

    private val viewModel: HistoryViewModel by lazy {
        ViewModelProvider(this).get(HistoryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_history)

        adapter = HistoryAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private var adapter: HistoryAdapter? = null
    override fun onStart() {
        super.onStart()

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter?.submitList(items)
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is History) {
            when (action) {
                BaseListAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t)
                    val snackbar = Snackbar.make(recyclerView, R.string.feedback_history_removed,
                        Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.button_undo) {
                        viewModel.insert(t)
                    }
                    snackbar.show()
                }
                BaseListAdapter.ActionListener.Action.SELECT -> {
                }
                BaseListAdapter.ActionListener.Action.MODIFY -> {
                }
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