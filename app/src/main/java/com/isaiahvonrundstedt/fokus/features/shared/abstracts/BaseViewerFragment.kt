package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.isaiahvonrundstedt.fokus.R

abstract class BaseViewerFragment(private val manager: FragmentManager) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Fokus_Component_Viewer)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.run {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setWindowAnimations(R.style.Fokus_Animations_Slide)
        }
    }

    fun show() {
        show(manager, this::class.java.simpleName)
    }
}