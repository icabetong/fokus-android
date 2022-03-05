package com.isaiahvonrundstedt.fokus.features.event.editor

import android.os.Bundle
import com.isaiahvonrundstedt.fokus.databinding.ActivityContainerEventBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity acts as a container
 * for the editor fragment. This is
 * used when needing to show the
 * editor ui without needing a fragment
 * transaction.
 */
@AndroidEntryPoint
class EventEditorContainer : BaseActivity() {
    private lateinit var binding: ActivityContainerEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}