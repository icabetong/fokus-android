package com.isaiahvonrundstedt.fokus.features.core.extensions

import android.graphics.Paint
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

/**
 *   Extension function used to change the text color
 *   of a AppCompatTextView
 *   @param id a color id from the Android resource
 */
fun TextView.setTextColorFromResource(@ColorRes id: Int) {
    this.setTextColor(ContextCompat.getColor(this.context, id))
}

/**
 *   Extension function used to add a strike through effect on
 *   the AppCompatTextView
 */
fun TextView.addStrikeThroughEffect() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

/**
 *   Extension function used to remove a strike through effect on
 *   the AppCompatTextView
 */
fun TextView.removeStrikeThroughEffect() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG.inv()
}