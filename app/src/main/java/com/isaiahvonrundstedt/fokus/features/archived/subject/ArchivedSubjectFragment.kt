package com.isaiahvonrundstedt.fokus.features.archived.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.databinding.FragmentArchivedSubjectBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivedSubjectFragment: BaseFragment() {
    private var _binding: FragmentArchivedSubjectBinding? = null

    private val archivedSubjectAdapter = ArchivedSubjectAdapter()
    private val viewModel: ArchivedSubjectViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentArchivedSubjectBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = archivedSubjectAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.items.observe(viewLifecycleOwner) {
            archivedSubjectAdapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}