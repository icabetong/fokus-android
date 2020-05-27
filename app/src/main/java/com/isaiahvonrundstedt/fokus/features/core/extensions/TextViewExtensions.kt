package com.isaiahvonrundstedt.fokus.features.core.extensions

import android.graphics.Paint
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

fun AppCompatTextView.setTextColorFromResource(@ColorRes id: Int) {
    this.setTextColor(ContextCompat.getColor(this.context, id))
}

fun AppCompatTextView.addStrikeThroughEffect() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

fun AppCompatTextView.removeStrikeThroughEffect() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG.inv()
}