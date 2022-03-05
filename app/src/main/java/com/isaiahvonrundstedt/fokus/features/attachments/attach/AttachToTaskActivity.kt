package com.isaiahvonrundstedt.fokus.features.attachments.attach

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.extensions.android.createToast
import com.isaiahvonrundstedt.fokus.databinding.ActivityAttachToTaskBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AttachToTaskActivity : BaseActivity(), BaseAdapter.SelectListener {
    private lateinit var binding: ActivityAttachToTaskBinding

    private val attachAdapter = AttachToTaskAdapter(this)
    private val viewModel: AttachToTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttachToTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)
        setToolbarTitle(R.string.sharing_attach_to_task)

        intent?.also {
            viewModel.subject = it.getStringExtra(Intent.EXTRA_SUBJECT)
            viewModel.url = it.getStringExtra(Intent.EXTRA_TEXT)
        }

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = attachAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        binding.titleView.text = viewModel.subject
        binding.summaryView.text = viewModel.url

        viewModel.tasks.observe(this) {
            attachAdapter.submitList(it)
        }
    }

    override fun <T> onItemSelected(t: T) {
        if (t is TaskPackage) {
            viewModel.addAttachment(t.task.taskID)
            createToast(R.string.feedback_attachment_added)
            finish()
        }
    }

}