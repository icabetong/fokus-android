package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import kotlinx.android.synthetic.main.fragment_event.*

class EventFragment : BaseFragment(), BaseListAdapter.ActionListener {

    private val viewModel: EventViewModel by lazy {
        ViewModelProvider(this).get(EventViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter(this)
        recyclerView.addItemDecoration(ItemDecoration(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.fetch()?.observe(viewLifecycleOwner, Observer { items ->
            adapter?.submitList(items)
            emptyView.isVisible = items.isEmpty()
        })
    }

    private var adapter: EventAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, EventEditor::class.java),
                EventEditor.REQUEST_CODE_INSERT)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is EventResource) {
            when (action) {
                // Show up the editorUI and pass the extra
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, EventEditor::class.java).apply {
                        putExtra(EventEditor.EXTRA_EVENT, t.event)
                        putExtra(EventEditor.EXTRA_SUBJECT, t.subject)
                    }
                    startActivityWithTransition(views, intent, EventEditor.REQUEST_CODE_UPDATE)
                }
                // Item has been swiped, notify database for deletion
                BaseListAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.event)

                    createSnackbar(R.string.feedback_event_removed, recyclerView).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.event) }
                    }

                    createSnackbar(R.string.feedback_event_removed, recyclerView).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.event) }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == EventEditor.REQUEST_CODE_INSERT ||
            requestCode == EventEditor.REQUEST_CODE_UPDATE) {
            val event: Event? = data?.getParcelableExtra(EventEditor.EXTRA_EVENT)

            event?.also {
                when (requestCode) {
                    EventEditor.REQUEST_CODE_INSERT ->
                        viewModel.insert(it)
                    EventEditor.REQUEST_CODE_UPDATE ->
                        viewModel.update(it)
                }
            }

        }
    }
}