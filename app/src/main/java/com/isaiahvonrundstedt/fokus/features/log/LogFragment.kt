package com.isaiahvonrundstedt.fokus.features.log

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.databinding.FragmentLogsBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogFragment : BaseFragment(), BaseAdapter.ActionListener {
    private var _binding: FragmentLogsBinding? = null
    private var controller: NavController? = null

    private val logAdapter = LogAdapter(this)
    private val viewModel: LogViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = Navigation.findNavController(requireActivity(), R.id.navigationHostFragment)
        binding.appBarLayout.toolbar.setTitle(R.string.activity_logs)
        setupNavigation(binding.appBarLayout.toolbar, controller)

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = logAdapter

            ItemTouchHelper(ItemSwipeCallback(context, logAdapter))
                .attachToRecyclerView(this)
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.logs.observe(this) { logAdapter.submitList(it) }
        viewModel.isEmpty.observe(this) { binding.emptyView.isVisible = it }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       container: View?) {
        if (t is Log) {
            when (action) {
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t)
                    val snackbar = Snackbar.make(binding.recyclerView, R.string.feedback_log_removed,
                        Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.button_undo) {
                        viewModel.insert(t)
                    }
                    snackbar.show()
                }
                BaseAdapter.ActionListener.Action.SELECT -> { }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logs, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_items -> {
                viewModel.removeLogs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}