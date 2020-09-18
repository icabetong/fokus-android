package com.isaiahvonrundstedt.fokus.components.extensions.android

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.ColorRes
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
 *  Extension function used to set an strike through effect on the
 *  painted text on the view
 *  @param status determines whether to add or remove the effect on the text
 */
fun TextView.setStrikeThroughEffect(status: Boolean) {
    if (status) this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    else this.paintFlags = this.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

/**
 *  Extension function used to add a compound drawable in the
 *  TextView at a specific position
 */
fun TextView.setCompoundDrawableAtStart(drawable: Drawable?) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
}

/**
 *  Extension function used to get the compound drawable in the
 *  TextView at the specific position
 */
fun TextView.getCompoundDrawableAtStart(): Drawable? {
    // Start, Top, End, Bottom
    return this.compoundDrawablesRelative[0]
}

/**
 *  Extension function to remove the compound drawable
 *  in the TextView at the specific position
 */
fun TextView.removeCompoundDrawableAtStart() {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
}