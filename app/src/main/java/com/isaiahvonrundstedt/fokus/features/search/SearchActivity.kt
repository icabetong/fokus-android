package com.isaiahvonrundstedt.fokus.features.search

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskBottomSheet
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_appbar_search.*

class SearchActivity: BaseActivity(), BaseAdapter.ActionListener {

    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupAppBar(toolbar, null)
    }

    override fun onStart() {
        super.onStart()

        val adapter = SearchAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.items.observe(this, Observer { items ->
            adapter.setObservableItems(items)
            searchEmptyView.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        })

        searchEditText.requestFocusFromTouch()
        searchEditText.doOnTextChanged { text, _, _, _ ->
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
                    TaskBottomSheet(t).invoke(supportFragmentManager)
                }
                BaseAdapter.ActionListener.Action.MODIFY -> { }
            }
        }
    }
}