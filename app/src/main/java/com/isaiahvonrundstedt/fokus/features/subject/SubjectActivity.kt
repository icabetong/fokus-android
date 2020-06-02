package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import kotlinx.android.synthetic.main.activity_subject.*
import kotlinx.android.synthetic.main.layout_appbar.*

class SubjectActivity: BaseActivity(), BaseAdapter.ActionListener {

    companion object {
        const val action = "com.isaiahvonrundstedt.fokus.features.subject.SubjectActivity.new"
    }

    private val viewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_subjects)

        // Check the intent action if we have been launched
        // from a launcher shortcut
        if (intent?.action == action)
            startActivityForResult(Intent(this, SubjectEditorActivity::class.java),
                SubjectEditorActivity.insertRequestCode)

        adapter = SubjectAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
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

        if (launchNextActivity) {
            actionButton.performClick()
            launchNextActivity = true
        }

        actionButton.setOnClickListener {
            startActivityForResult(Intent(this, SubjectEditorActivity::class.java),
                SubjectEditorActivity.insertRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T,
                                       action: BaseAdapter.ActionListener.Action,
                                       itemView: View) {
        if (t is Subject) {
            when (action) {
                // Create the intent for the editorUI and pass the extras
                // and wait for the result
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        itemView, BaseEditor.transitionName)

                    val editorIntent = Intent(this, SubjectEditorActivity::class.java)
                    editorIntent.putExtra(SubjectEditorActivity.extraSubject, t)
                    startActivityForResult(editorIntent, SubjectEditorActivity.updateRequestCode,
                        options.toBundle())
                }
                // Item has been swiped from the RecyclerView, notify user action
                // in the ViewModel to delete it from the database
                // then show a SnackBar feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t)
                    val snackbar = Snackbar.make(recyclerView, R.string.feedback_subject_removed,
                        Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.button_undo) {
                        viewModel.insert(t)
                    }
                    snackbar.show()
                }
                BaseAdapter.ActionListener.Action.MODIFY -> {}
            }
        }
    }

    private var launchNextActivity: Boolean = false
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            launchNextActivity = true
            val subject: Subject = data?.getParcelableExtra(SubjectEditorActivity.extraSubject)!!

            if (requestCode == SubjectEditorActivity.insertRequestCode) {
                viewModel.insert(subject)

                // The user has added a subject, now disable the
                // dialog for first run
                if (PreferenceManager(this).isFirstRun)
                    PreferenceManager(this).isFirstRun = false
            } else if (requestCode == SubjectEditorActivity.updateRequestCode) {
                viewModel.update(subject)
            }
        }
    }

}