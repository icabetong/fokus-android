package com.isaiahvonrundstedt.fokus.components.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.isaiahvonrundstedt.fokus.R

class ReactiveTextColorSwitch @JvmOverloads constructor (
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.switchStyle
): SwitchCompat(context, attributeSet, defStyleAttr) {

    init {
        setSwitchTextAppearance(context, R.style.Fokus_TextAppearance_Editor_Value)
        setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                this.setTextColorRes(R.color.color_primary_text)
            else this.setTextColorRes(R.color.color_secondary_text)
        }
    }

    private fun setTextColorRes(@ColorRes resId: Int) {
        setTextColor(ContextCompat.getColor(context, resId))
    }
}