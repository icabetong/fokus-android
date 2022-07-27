package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.MaterialToolbar
import com.isaiahvonrundstedt.fokus.R

abstract class BasePickerFragment(private val manager: FragmentManager): DialogFragment() {

    private var toolbar: MaterialToolbar? = null

    protected fun setupToolbar(toolbar: MaterialToolbar,
                               @StringRes titleRes: Int = 0,
                               @MenuRes menuRes: Int = 0,
                               onNavigate: (() -> Unit)? = null,
                               onMenuItemClicked: ((id: Int) -> Unit)? = null) {
        this.toolbar = toolbar
        with(toolbar) {
            setTitle(titleRes)
            setNavigationIcon(R.drawable.ic_outline_close_24)
            setNavigationOnClickListener { if (onNavigate != null) onNavigate() }
            setOnMenuItemClickListener {
                if (onMenuItemClicked != null) onMenuItemClicked(it.itemId);
                true
            }
            if (menuRes != 0) inflateMenu(menuRes)
        }
    }
    
    fun show() {
        if (!isAdded || !isVisible) {
            show(manager, this::class.java.simpleName)
        }
    }

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
}