package com.isaiahvonrundstedt.fokus.features.shared.components.sheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.subject.SubjectActivity
import kotlinx.android.synthetic.main.layout_sheet_first_run.*

class FirstRunBottomSheet: BaseBottomSheet() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_first_run, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionButton.setOnClickListener {
            this.dismiss()
            startActivity(Intent(context, SubjectActivity::class.java).apply {
                action = SubjectActivity.action
            })
        }
    }
}