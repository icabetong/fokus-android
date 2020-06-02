package com.isaiahvonrundstedt.fokus.features.notifications

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
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.layout_appbar.*

class NotificationActivity: BaseActivity(), BaseAdapter.ActionListener {

    private val viewModel: NotificationViewModel by lazy {
        ViewModelProvider(this).get(NotificationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_notifications)

        adapter = NotificationAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private var adapter: NotificationAdapter? = null
    override fun onStart() {
        super.onStart()

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun <T> onActionPerformed(t: T,
                                       action: BaseAdapter.ActionListener.Action,
                                       itemView: View) {
        if (t is Notification) {
            when (action) {
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t)
                    val snackbar = Snackbar.make(recyclerView, R.string.feedback_notification_removed,
                        Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.button_undo) {
                        viewModel.insert(t)
                    }
                    snackbar.show()
                }
                BaseAdapter.ActionListener.Action.SELECT -> { }
                BaseAdapter.ActionListener.Action.MODIFY -> { }
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