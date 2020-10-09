package com.isaiahvonrundstedt.fokus.features.attachments.send

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.activity_send_attachment.*
import kotlinx.android.synthetic.main.layout_appbar.*

class SendAsAttachmentActivity: BaseActivity(), SendAsAttachmentAdapter.ActionListener {

    private val sendAdapter = SendAsAttachmentAdapter(this)
    private val viewModel by lazy {
        ViewModelProvider(this).get(SendAsAttachmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_attachment)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.intent_add_as_attachment)

        intent?.also {
            viewModel.subject = it.getStringExtra(Intent.EXTRA_SUBJECT)
            viewModel.url = it.getStringExtra(Intent.EXTRA_TEXT)
        }

        with(recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = sendAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        titleView.text = viewModel.subject
        summaryView.text = viewModel.url
        viewModel.tasks.observe(this) { sendAdapter.submitList(it) }
    }

    override fun onAction(action: SendAsAttachmentAdapter.ActionListener.Action, taskID: String) {
        when (action) {
            SendAsAttachmentAdapter.ActionListener.Action.ADD -> {
                viewModel.addAttachment(taskID)
                createSnackbar(R.string.feedback_attachment_added)
            }
            SendAsAttachmentAdapter.ActionListener.Action.REMOVE -> {
                viewModel.removeAttachment()
                createSnackbar(R.string.feedback_attachment_removed)
            }
        }
    }

}