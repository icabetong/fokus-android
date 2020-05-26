package com.isaiahvonrundstedt.fokus.features.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskEditorActivity
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_appbar_search.*

class SearchActivity: BaseActivity(), BaseAdapter.ActionListener {

    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setPersistentActionBar(toolbar)
    }

    override fun onStart() {
        super.onStart()

        val adapter = SearchAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.items.observe(this, Observer { items ->
            adapter.submitList(items)
            recyclerView.isVisible = items.isNotEmpty()
            searchEmptyView.isVisible = items.isEmpty()
        })

        searchEditText.requestFocusFromTouch()
        searchEditText.doAfterTextChanged { text ->
            recyclerView.isVisible = text?.isNotBlank() == true
            searchEmptyView.isVisible = text.isNullOrBlank()
            viewModel.fetch(text.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Core) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val editor = Intent(this, TaskEditorActivity::class.java)
                    editor.putExtra(TaskEditorActivity.extraTask, t.task)
                    editor.putExtra(TaskEditorActivity.extraSubject, t.subject)
                    editor.putParcelableArrayListExtra(TaskEditorActivity.extraAttachments,
                        ArrayList(t.attachmentList))
                    startActivityForResult(editor, TaskEditorActivity.updateRequestCode)
                }
                BaseAdapter.ActionListener.Action.MODIFY -> { }
                BaseAdapter.ActionListener.Action.DELETE -> { }
            }
        }
    }
}