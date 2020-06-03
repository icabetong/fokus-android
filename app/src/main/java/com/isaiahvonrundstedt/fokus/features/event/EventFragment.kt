package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import kotlinx.android.synthetic.main.fragment_event.*

class EventFragment: BaseFragment(), BaseAdapter.ActionListener {

    companion object {
        const val action = "com.isaiahvonrundstedt.fokus.features.event.new"
    }

    private val viewModel: EventViewModel? by lazy {
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

        viewModel?.fetch()?.observe(viewLifecycleOwner, Observer { items ->
            adapter?.submitList(items)
            itemEmptyView.isVisible = items.isEmpty()
        })
    }

    private var adapter: EventAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, EventEditor::class.java),
                EventEditor.updateRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Event) {
            when (action) {
                // Show up the editorUI and pass the extra
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, EventEditor::class.java).apply {
                        putExtra(EventEditor.extraEvent, t)
                    }
                    startEditorWithTransition(views, intent, EventEditor.updateRequestCode)
                }
                // Item has been swiped, notify database for deletion
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel?.remove(t)
                    val snackbar = Snackbar.make(recyclerView, R.string.feedback_event_removed,
                        Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.button_undo) {
                        viewModel?.insert(t)
                    }
                    snackbar.show()
                }
                BaseAdapter.ActionListener.Action.MODIFY -> { }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val event: Event = data?.getParcelableExtra(EventEditor.extraEvent)!!

            if (requestCode == EventEditor.insertRequestCode) {
                viewModel?.insert(event)
            } else if (requestCode == EventEditor.updateRequestCode) {
                viewModel?.update(event)
            }
        }
    }
}