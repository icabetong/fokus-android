package com.isaiahvonrundstedt.fokus.components.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatRadioButton
import com.isaiahvonrundstedt.fokus.R

open class RadioButtonCompat @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.radioButtonStyle
) : AppCompatRadioButton(context, attributeSet, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.RadioButtonCompat,
            defStyleAttr,
            0
        )

        try {
            val textAppearance = typedArray.getResourceId(
                R.styleable.RadioButtonCompat_textAppearanceCompat,
                R.style.Fokus_TextAppearance_Body_Medium
            )
            setTextAppearanceCompat(textAppearance)
        } catch (e: Exception) {
        }
        typedArray.recycle()
    }

    @Suppress("DEPRECATION")
    fun setTextAppearanceCompat(@StyleRes resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            this.setTextAppearance(resId)
        else this.setTextAppearance(context, resId)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setPaddingRelative(16, 0, 16, 0)
    }

}