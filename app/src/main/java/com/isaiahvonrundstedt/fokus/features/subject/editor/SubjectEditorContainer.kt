package com.isaiahvonrundstedt.fokus.features.subject.editor

import android.os.Bundle
import com.isaiahvonrundstedt.fokus.databinding.ActivityContainerSubjectBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectEditorContainer: BaseActivity() {
    private lateinit var binding: ActivityContainerSubjectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}