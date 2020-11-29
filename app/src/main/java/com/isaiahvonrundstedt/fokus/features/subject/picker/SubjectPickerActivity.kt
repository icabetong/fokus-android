package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.android.putExtra
import com.isaiahvonrundstedt.fokus.databinding.ActivitySubjectPickerBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectPickerActivity: BaseActivity(), BaseAdapter.ActionListener,
    SubjectPickerAdapter.ItemLongPressListener {

    private lateinit var binding: ActivitySubjectPickerBinding

    private val pickerAdapter = SubjectPickerAdapter(this, this)
    private val viewModel by lazy {
        ViewModelProvider(this).get(SubjectPickerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)
        setToolbarTitle(R.string.dialog_assign_subject)

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = pickerAdapter
        }

        ItemTouchHelper(ItemSwipeCallback(this, pickerAdapter))
            .attachToRecyclerView(binding.recyclerView)
    }

    override fun onStart() {
        super.onStart()

        viewModel.subjects.observe(this) { pickerAdapter.submitList(it) }
        viewModel.isEmpty.observe(this) { binding.emptyView.isVisible = it }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       container: View?) {
        if (t is SubjectPackage) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    setResult(RESULT_OK, Intent().apply{
                        putExtra(EXTRA_SELECTED_SUBJECT, t)
                    })
                    finish()
                }
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.subject)

                    createSnackbar(R.string.feedback_subject_removed).run {
                        setAction(R.string.button_undo) {
                            viewModel.insert(t.subject, t.schedules)
                        }
                    }
                }
            }
        }
    }

    override fun onItemLongPressed(subjectPackage: SubjectPackage, container: View?) {
        val intent = Intent(this, SubjectEditor::class.java).apply {
            putExtra(SubjectEditor.EXTRA_SUBJECT, subjectPackage.subject)
            putExtra(SubjectEditor.EXTRA_SCHEDULE, subjectPackage.schedules)
        }

        container?.also {
            startActivityForResult(intent, SubjectEditor.REQUEST_CODE_UPDATE,
                buildTransitionOptions(it, it.transitionName))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add -> {
                startActivityForResult(Intent(this, SubjectEditor::class.java),
                    SubjectEditor.REQUEST_CODE_INSERT)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK)
            return

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == SubjectEditor.REQUEST_CODE_INSERT
            || requestCode == SubjectEditor.REQUEST_CODE_UPDATE) {

            val subject: Subject? = data?.getParcelableExtra(SubjectEditor.EXTRA_SUBJECT)
            val scheduleList: List<Schedule>? = data?.getParcelableListExtra(SubjectEditor.EXTRA_SCHEDULE)

            subject?.also {
                when (requestCode) {
                    SubjectEditor.REQUEST_CODE_INSERT ->
                        viewModel.insert(it, scheduleList ?: emptyList())
                    SubjectEditor.REQUEST_CODE_UPDATE ->
                        viewModel.update(it, scheduleList ?: emptyList())
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PICK = 2

        const val EXTRA_SELECTED_SUBJECT = "extra:subject"
    }
}