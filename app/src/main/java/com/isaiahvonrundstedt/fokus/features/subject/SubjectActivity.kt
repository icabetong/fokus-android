package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.features.shared.custom.OffsetItemDecoration
import kotlinx.android.synthetic.main.activity_subject.*
import kotlinx.android.synthetic.main.layout_appbar.*

class SubjectActivity: BaseActivity(), BaseAdapter.ActionListener, BaseAdapter.SwipeListener {

    companion object {
        const val action = "com.isaiahvonrundstedt.fokus.features.subject.SubjectActivity.new"
    }

    private val viewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)
        setPersistentActionBar(toolbar, R.string.activity_subjects)

        if (intent?.action == action)
            actionButton.performClick()

        adapter = SubjectAdapter(this, this)
        recyclerView.addItemDecoration(OffsetItemDecoration(this, R.dimen.item_padding))
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(this, adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private var adapter: SubjectAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            val editorIntent = Intent(this, SubjectEditorActivity::class.java)
            startActivityForResult(editorIntent, SubjectEditorActivity.insertRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Subject) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val editorIntent = Intent(this, SubjectEditorActivity::class.java)
                    editorIntent.putExtra(SubjectEditorActivity.extraSubject, t)
                    startActivityForResult(editorIntent, SubjectEditorActivity.updateRequestCode)
                }
                BaseAdapter.ActionListener.Action.MODIFY -> {}
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val subject: Subject = data?.getParcelableExtra(SubjectEditorActivity.extraSubject)!!

            if (requestCode == SubjectEditorActivity.insertRequestCode) {
                viewModel.insert(subject)
            } else if (requestCode == SubjectEditorActivity.updateRequestCode) {
                viewModel.update(subject)
            }
        }
    }

    override fun <T> onSwipePerformed(position: Int, t: T, swipeDirection: Int) {
        if (t is Subject) {
            if (swipeDirection == ItemTouchHelper.START) {
                viewModel.remove(t)
                val snackbar = Snackbar.make(recyclerView, R.string.feedback_subject_removed,
                    Snackbar.LENGTH_SHORT)
                snackbar.setAction(R.string.button_undo) {
                    viewModel.insert(t)
                }
                snackbar.show()
            }
        }
    }

}