package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.isaiahvonrundstedt.fokus.R

abstract class BaseBottomSheet<T>(private val manager: FragmentManager)
    : BottomSheetDialogFragment() {

    protected var receiver: Receiver<T>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

    fun waitForResult(receiver: Receiver<T>) {
        this.receiver = receiver
    }

    fun interface Receiver<T> {
        fun onReceive(t: T)
    }

    fun show() {
        if (!this.isAdded || !this.isVisible)
            show(manager, this::class.java.name)
    }

    inline fun show(sheet: BaseBottomSheet<T>.() -> Unit) {
        this.sheet()
        this.show()
    }
}