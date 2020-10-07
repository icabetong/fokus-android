package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.isaiahvonrundstedt.fokus.R

abstract class BaseBottomSheet<T>(private val manager: FragmentManager)
    : BottomSheetDialogFragment() {

    protected var receiver: Receiver<T>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            with(BottomSheetBehavior.from(bottomSheet)) {
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
            = BottomSheetDialog(requireContext(), theme)

    fun waitForResult(receiver: Receiver<T>) {
        this.receiver = receiver
    }

    fun show() {
        if (!this.isAdded || !this.isVisible)
            show(manager, this::class.java.name)
    }

    inline fun show(sheet: BaseBottomSheet<T>.() -> Unit) {
        this.sheet()
        this.show()
    }

    fun interface CancelListener {
        fun onCancel()
    }

    fun interface Receiver<T> {
        fun onReceive(t: T)
    }
}