package com.isaiahvonrundstedt.fokus.components.custom

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatRadioButton
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R

open class RadioButtonCompat @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.radioButtonStyle
): AppCompatRadioButton(context, attributeSet, defStyleAttr) {

    @Suppress("DEPRECATION")
    fun setTextAppearanceCompat(@StyleRes resId: Int) {
        if (CoreApplication.isRunningAtVersion(23))
            this.setTextAppearance(resId)
        else this.setTextAppearance(context, resId)
    }
}