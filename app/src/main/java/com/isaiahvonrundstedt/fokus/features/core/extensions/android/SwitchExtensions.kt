package com.isaiahvonrundstedt.fokus.features.core.extensions.android

import android.widget.Switch
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.isaiahvonrundstedt.fokus.R

fun Switch.changeTextColorWhenChecked() {
    this.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) this.setTextColorFromResource(R.color.colorPrimaryText)
        else this.setTextColorFromResource(R.color.colorSecondaryText)
    }
}

fun Switch.setTextColorFromResource(@ColorRes id: Int) {
    this.setTextColor(ContextCompat.getColor(this.context, id))
}