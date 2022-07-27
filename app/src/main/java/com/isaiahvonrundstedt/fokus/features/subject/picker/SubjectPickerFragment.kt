package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.databinding.FragmentPickerSubjectBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePickerFragment
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectPickerFragment(fragmentManager: FragmentManager): BasePickerFragment(fragmentManager),
    BaseAdapter.ActionListener {
    private var _binding: FragmentPickerSubjectBinding? = null

    private val binding get() = _binding!!
    private val pickerAdapter = SubjectPickerAdapter(this)
    private val viewModel: SubjectPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.appBarLayout.toolbar, R.string.dialog_assign_subject, onNavigate = { dismiss() })

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

    override fun <T> onActionPerformed(
        t: T, action: BaseAdapter.ActionListener.Action,
        container: View?
    ) {
        if (t is SubjectPackage) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    setFragmentResult(REQUEST_KEY_PICK,
                        bundleOf(EXTRA_SELECTED_SUBJECT to t))
                    dismiss()
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
        const val REQUEST_KEY_PICK = "request:pick:subject"
        const val EXTRA_SELECTED_SUBJECT = "extra:subject"
    }
}