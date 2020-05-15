package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R

abstract class BaseBottomSheet: BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.AppTheme_BottomSheet

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
        = BottomSheetDialog(requireContext(), theme)

    fun invoke(fragmentManager: FragmentManager) {
        if (!this.isAdded)
            show(fragmentManager, this::class.java.name)
    }

    interface DismissListener {
        fun <E> onDismiss(status: Int, mode: Int, e: E)
    }

    protected var status: Int = statusDiscard
    companion object {
        const val modeInsert = 1
        const val modeUpdate = 2

        const val statusDiscard = 0
        const val statusCommit = 1
    }

    protected fun showFeedback(view: View, @StringRes id: Int) {
        Snackbar.make(view, id, Snackbar.LENGTH_SHORT).show()
    }
}