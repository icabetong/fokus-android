package com.isaiahvonrundstedt.fokus.components.extensions.android

import androidx.annotation.ColorRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.isaiahvonrundstedt.fokus.R

fun SwitchCompat.changeTextColorWhenChecked() {
    this.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) this.setTextColorFromResource(R.color.colorPrimaryText)
        else this.setTextColorFromResource(R.color.colorSecondaryText)
    }
}

fun SwitchCompat.setTextColorFromResource(@ColorRes id: Int) {
    this.setTextColor(ContextCompat.getColor(this.context, id))
}