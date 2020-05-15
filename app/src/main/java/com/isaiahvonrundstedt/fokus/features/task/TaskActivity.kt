package com.isaiahvonrundstedt.fokus.features.task

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.components.menu.NavigationBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.features.shared.custom.OffsetItemDecoration
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.layout_appbar.*

class TaskActivity: BaseActivity(), BaseBottomSheet.DismissListener, BaseAdapter.ActionListener,
    BaseAdapter.SwipeListener {

    companion object {
        private const val action = "com.isaiahvonrundstedt.fokus.features.task.TaskActivity.new"
    }

    private val viewModel: TaskViewModel by lazy {
        ViewModelProvider(this).get(TaskViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        setupAppBar(toolbar, R.string.activity_main)

        if (intent?.action == action)
            TaskBottomSheet(this).invoke(supportFragmentManager)

        toolbar?.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_android_menu)
        toolbar?.setNavigationOnClickListener {
            NavigationBottomSheet().invoke(supportFragmentManager)
        }

        adapter = TaskAdapter(this, this)
        recyclerView.addItemDecoration(OffsetItemDecoration(this, R.dimen.item_padding))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private var adapter: TaskAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            TaskBottomSheet(this).invoke(supportFragmentManager)
        }

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter?.setObservableItems(items.toList())
            itemEmptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Core) {
            when (action) {
                BaseAdapter.ActionListener.Action.MODIFY -> {
                    viewModel.update(t)
                    if (t.task.isFinished) {
                        if (PreferenceManager(this).completedSounds) {
                            Snackbar.make(recyclerView, R.string.feedback_task_marked_as_finished,
                                Snackbar.LENGTH_SHORT).show()
                            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            RingtoneManager.getRingtone(this.applicationContext, soundUri).play()
                        }
                        if (PreferenceManager(this).autoArchive) {
                            t.task.isArchived = true
                            viewModel.update(t)
                        }
                    }
                }
                BaseAdapter.ActionListener.Action.SELECT ->
                    TaskBottomSheet(t, this).invoke(supportFragmentManager)
            }
        }
    }

    override fun <T> onSwipePerformed(position: Int, t: T, swipeDirection: Int) {
        if (t is Core) {
            if (swipeDirection == ItemTouchHelper.START) {
                viewModel.remove(t)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_task_removed,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    viewModel.insert(t)
                }
                snackbar.show()
            } else if (swipeDirection == ItemTouchHelper.END) {
                t.task.isArchived = true
                viewModel.update(t)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_task_archived,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    t.task.isArchived = false
                    viewModel.insert(t)
                }
                snackbar.show()
            }
        }
    }

    override fun <E> onDismiss(status: Int, mode: Int, e: E) {
        if (e is Core && status == BaseBottomSheet.statusCommit) {
            if (mode == BaseBottomSheet.modeInsert)
                viewModel.insert(e)
             else viewModel.update(e)
        }
    }

}
