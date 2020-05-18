package com.isaiahvonrundstedt.fokus.features.task

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.components.menu.NavigationBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.features.shared.custom.OffsetItemDecoration
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.layout_appbar.*

class TaskActivity: BaseActivity(), BaseAdapter.ActionListener,
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
        setPersistentActionBar(toolbar, R.string.activity_main)

        if (intent?.action == action)
            actionButton.performClick()

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

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private var adapter: TaskAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            val editorIntent = Intent(this, TaskEditorActivity::class.java)
            startActivityForResult(editorIntent, TaskEditorActivity.insertRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Core) {
            when (action) {
                BaseAdapter.ActionListener.Action.MODIFY -> {
                    viewModel.update(t.task)
                    if (t.task.isFinished) {
                        if (PreferenceManager(this).completedSounds) {
                            Snackbar.make(recyclerView, R.string.feedback_task_marked_as_finished,
                                Snackbar.LENGTH_SHORT).show()
                            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            RingtoneManager.getRingtone(this.applicationContext, soundUri).play()
                        }
                        if (PreferenceManager(this).autoArchive) {
                            t.task.isArchived = true
                            viewModel.update(t.task)
                        }
                    }
                }
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val editorIntent = Intent(this, TaskEditorActivity::class.java)
                    editorIntent.putExtra(TaskEditorActivity.extraSubject, t.subject)
                    editorIntent.putExtra(TaskEditorActivity.extraTask, t.task)
                    editorIntent.putParcelableArrayListExtra(TaskEditorActivity.extraAttachments, ArrayList(t.attachmentList))
                    startActivityForResult(editorIntent, TaskEditorActivity.updateRequestCode)
                }
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
                t.task.isArchived = true
                viewModel.update(t.task, t.attachmentList)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_task_archived,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    t.task.isArchived = false
                    viewModel.insert(t.task, t.attachmentList)
                }
                snackbar.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val task: Task = data?.getParcelableExtra(TaskEditorActivity.extraTask)!!
            val attachments: List<Attachment> = data.getParcelableArrayListExtra(TaskEditorActivity.extraAttachments)!!

            if (requestCode == TaskEditorActivity.insertRequestCode) {
                viewModel.insert(task, attachments)
            } else if (requestCode == TaskEditorActivity.updateRequestCode) {
                viewModel.update(task, attachments)
            }
        }
    }
}
