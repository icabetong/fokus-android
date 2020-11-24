package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.databinding.ActivitySubjectPickerBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectPickerActivity: BaseActivity(), BaseAdapter.ActionListener {

    private lateinit var binding: ActivitySubjectPickerBinding

    private val pickerAdapter = SubjectPickerAdapter(this)
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
            layoutManager = LinearLayoutManager(context)
            adapter = pickerAdapter
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
                BaseAdapter.ActionListener.Action.DELETE -> { }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PICK = 2

        const val EXTRA_SELECTED_SUBJECT = "extra:subject"
    }
}