package com.isaiahvonrundstedt.fokus.features.event.editor

import android.os.Bundle
import com.isaiahvonrundstedt.fokus.databinding.ActivityContainerEventBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventEditorContainer: BaseActivity() {
    private lateinit var binding: ActivityContainerEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}