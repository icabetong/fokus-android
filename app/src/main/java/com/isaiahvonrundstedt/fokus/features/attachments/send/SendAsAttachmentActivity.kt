package com.isaiahvonrundstedt.fokus.features.attachments.send

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.extensions.android.createToast
import com.isaiahvonrundstedt.fokus.databinding.ActivitySendAttachmentBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity

class SendAsAttachmentActivity: BaseActivity(), SendAsAttachmentAdapter.ShareListener {

    private val sendAdapter = SendAsAttachmentAdapter(this)
    private val viewModel by lazy {
        ViewModelProvider(this).get(SendAsAttachmentViewModel::class.java)
    }

    private lateinit var binding: ActivitySendAttachmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendAttachmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)
        setToolbarTitle(R.string.intent_add_as_attachment)

        intent?.also {
            viewModel.subject = it.getStringExtra(Intent.EXTRA_SUBJECT)
            viewModel.url = it.getStringExtra(Intent.EXTRA_TEXT)
        }

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = sendAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        binding.titleView.text = viewModel.subject
        binding.summaryView.text = viewModel.url
        viewModel.tasks.observe(this) { sendAdapter.submitList(it) }
    }

    override fun onShareToTask(taskID: String) {
        viewModel.addAttachment(taskID)
        createToast(R.string.feedback_attachment_added)
        finish()
    }
}