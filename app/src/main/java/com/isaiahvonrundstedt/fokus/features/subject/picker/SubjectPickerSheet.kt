package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.databinding.LayoutSheetSubjectBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel

class SubjectPickerSheet(fragmentManager: FragmentManager)
    : BaseBottomSheet<SubjectPackage>(fragmentManager), BaseAdapter.ActionListener {

    private var _binding: LayoutSheetSubjectBinding? = null

    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = LayoutSheetSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pickerAdapter = SubjectPickerAdapter(this)
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = pickerAdapter
        }

        viewModel.subjects.observe(viewLifecycleOwner) { pickerAdapter.submitList(it) }
        viewModel.isEmpty.observe(viewLifecycleOwner) { binding.emptyView.isVisible = it }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is SubjectPackage) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    receiver?.onReceive(t)
                    this.dismiss()
                }
                BaseAdapter.ActionListener.Action.DELETE -> { }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}