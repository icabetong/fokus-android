package com.isaiahvonrundstedt.fokus.features.event

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import kotlinx.android.synthetic.main.fragment_event.*

class EventFragment: BaseFragment(), BaseAdapter.ActionListener {

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
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
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
                EventEditor.insertRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is EventResource) {
            when (action) {
                // Show up the editorUI and pass the extra
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, EventEditor::class.java).apply {
                        putExtra(EventEditor.extraEvent, t.event)
                        putExtra(EventEditor.extraSubject, t.subject)
                    }
                    startActivityWithTransition(views, intent, EventEditor.updateRequestCode)
                }
                // Item has been swiped, notify database for deletion
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.event)

                    createSnackbar(recyclerView, R.string.feedback_event_removed).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.event) }
                        show()
                    }
                }
                BaseAdapter.ActionListener.Action.MODIFY -> { }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == EventEditor.insertRequestCode ||
                requestCode == EventEditor.updateRequestCode) {

            if (resultCode == BaseEditor.RESULT_OK || resultCode == BaseEditor.RESULT_DELETE) {
                val event: Event? = data?.getParcelableExtra(EventEditor.extraEvent)

                event?.also {
                    if (resultCode == BaseEditor.RESULT_OK) {
                        if (requestCode == EventEditor.insertRequestCode)
                            viewModel.insert(it)
                        else viewModel.update(it)
                    } else {
                        viewModel.remove(it)

                        createSnackbar(recyclerView, R.string.feedback_event_removed).apply {
                            setAction(R.string.button_undo) { _ -> viewModel.insert(it) }
                            show()
                        }
                    }
                }
            }
        }
    }
}