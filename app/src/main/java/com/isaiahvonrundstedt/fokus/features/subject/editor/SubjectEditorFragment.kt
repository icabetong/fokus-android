package com.isaiahvonrundstedt.fokus.features.subject.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.ActivityEditorSubjectBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment

class SubjectEditorFragment: BaseFragment() {

    private var _binding: ActivityEditorSubjectBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = ActivityEditorSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}