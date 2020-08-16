package com.isaiahvonrundstedt.fokus.components.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.isaiahvonrundstedt.fokus.R
import kotlinx.android.synthetic.main.layout_dialog_progress.*

class ProgressDialog(private val manager: FragmentManager, @StringRes private val titleRes: Int): DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_dialog_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView.text = getString(titleRes)
    }

    fun show() {
        this.show(manager, this::class.java.simpleName)
    }
}