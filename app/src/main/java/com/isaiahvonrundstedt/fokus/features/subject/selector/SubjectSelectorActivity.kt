package com.isaiahvonrundstedt.fokus.features.subject.selector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import kotlinx.android.synthetic.main.fragment_subject.*
import kotlinx.android.synthetic.main.layout_appbar_selector.*

class SubjectSelectorActivity: BaseActivity(), BaseAdapter.ActionListener {

    private val viewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_selector_subject)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.dialog_select_subject)

        val adapter = SubjectSelectorAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.fetch()?.observe(this, Observer {
            adapter.submitList(it)
            emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_selector, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new -> {
                startActivityForResult(Intent(this, SubjectEditor::class.java),
                    SubjectEditor.insertRequestCode)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SubjectEditor.insertRequestCode && resultCode == Activity.RESULT_OK) {
            val subject: Subject? = data?.getParcelableExtra(SubjectEditor.extraSubject)
            subject?.let {
                viewModel.insert(it)
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Subject) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val result = Intent()
                    result.putExtra(extraSubject, t)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
                BaseAdapter.ActionListener.Action.MODIFY -> {}
                BaseAdapter.ActionListener.Action.DELETE -> {}
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_down)
    }

    companion object {
        const val requestCode = 43
        const val extraSubject = "extra:subject"
    }
}