package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.databinding.ActivitySubjectPickerBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectPickerActivity: BaseActivity(), BaseAdapter.ActionListener {

    private lateinit var binding: ActivitySubjectPickerBinding

    private val pickerAdapter = SubjectPickerAdapter(this)
    private val viewModel: SubjectPickerViewModel by viewModels()

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

            ItemTouchHelper(ItemSwipeCallback(context, pickerAdapter))
                .attachToRecyclerView(binding.recyclerView)
        }
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

    companion object {
        const val EXTRA_SELECTED_SUBJECT = "extra:subject"
    }
}