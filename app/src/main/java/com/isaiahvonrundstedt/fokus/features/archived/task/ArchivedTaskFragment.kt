package com.isaiahvonrundstedt.fokus.features.archived.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.databinding.FragmentArchivedTaskBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivedTaskFragment: BaseFragment() {
    private var _binding: FragmentArchivedTaskBinding? = null

    private val archivedTaskAdapter = ArchivedTaskAdapter()
    private val viewModel: ArchivedTaskViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentArchivedTaskBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = archivedTaskAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.items.observe(viewLifecycleOwner) {
            archivedTaskAdapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}