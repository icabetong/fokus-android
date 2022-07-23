package com.isaiahvonrundstedt.fokus.components.preference

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.R

@SuppressLint("RestrictedApi")
class InformationHolder(
    context: Context,
    attr: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : Preference(context, attr, defStyleAttr, defStyleRes) {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int)
            : this(context, attr, defStyleAttr, 0)

    constructor(context: Context, attr: AttributeSet?)
            : this(
        context, attr, TypedArrayUtils.getAttr(
            context,
            androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle
        )
    )

    constructor(context: Context) : this(context, null)

    init {
        layoutResource = R.layout.layout_preference_info
    }

    override fun onClick() {}
    override fun performClick() {}

}