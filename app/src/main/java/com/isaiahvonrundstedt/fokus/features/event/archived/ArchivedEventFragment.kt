package com.isaiahvonrundstedt.fokus.features.event.archived

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.databinding.FragmentArchivedEventBinding
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivedEventFragment : BaseFragment(), BaseAdapter.SelectListener {
    private var _binding: FragmentArchivedEventBinding? = null
    private var controller: NavController? = null

    private val archivedEventAdapter = ArchivedEventAdapter(this)
    private val viewModel: ArchivedEventViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchivedEventBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBarLayout.toolbar,
            arrayOf(binding.recyclerView, binding.emptyView))
        controller = Navigation.findNavController(view)

        with(binding.appBarLayout.toolbar) {
            setTitle(R.string.activity_archives)
            setNavigationIcon(R.drawable.ic_outline_arrow_back_24)
            setNavigationOnClickListener { controller?.navigateUp() }
        }

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = archivedEventAdapter
        }
    }

    override fun <T> onItemSelected(t: T) {
        if (t is EventPackage) {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                title(R.string.dialog_confirm_unarchive_title)
                message(R.string.dialog_confirm_unarchive_summary)
                positiveButton {
                    viewModel.removeFromArchive(t)
                }
                negativeButton(R.string.button_cancel)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.items.observe(viewLifecycleOwner) {
            archivedEventAdapter.submitList(it)
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