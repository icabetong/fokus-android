package com.isaiahvonrundstedt.fokus.features.archived.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.databinding.FragmentArchivedTaskBinding
import com.isaiahvonrundstedt.fokus.features.archived.ArchivedAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivedTaskFragment: BaseFragment(), ArchivedAdapter.ArchivedItemClickListener {
    private var _binding: FragmentArchivedTaskBinding? = null

    private val archivedTaskAdapter = ArchivedTaskAdapter(this)
    private val viewModel: ArchivedTaskViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
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

    override fun <T> onArchivedItemClicked(data: T) {
        if (data is TaskPackage) {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                title(R.string.dialog_confirm_unarchive_title)
                message(R.string.dialog_confirm_unarchive_summary)
                positiveButton {
                    viewModel.removeFromArchive(data)
                }
                negativeButton(R.string.button_cancel)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.items.observe(viewLifecycleOwner) {
            archivedTaskAdapter.submitList(it)
        }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            binding.emptyView.isVisible = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}