package com.isaiahvonrundstedt.fokus.features.archived.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.databinding.FragmentArchivedEventBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivedEventFragment: BaseFragment() {
    private var _binding: FragmentArchivedEventBinding? = null

    private val archivedEventAdapter = ArchivedEventAdapter()
    private val viewModel: ArchivedEventViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentArchivedEventBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = archivedEventAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.items.observe(viewLifecycleOwner) {
            archivedEventAdapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}