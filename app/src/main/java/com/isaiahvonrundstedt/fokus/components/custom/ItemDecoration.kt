package com.isaiahvonrundstedt.fokus.components.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration(private var context: Context,
                     @DimenRes private var v: Int, @DimenRes private var h: Int)
    : DividerItemDecoration(context, VERTICAL) {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val verticalOffset: Int = context.resources.getDimension(this.v).toInt()
        val horizontalOffset: Int = context.resources.getDimension(this.h).toInt()

        if (parent.getChildAdapterPosition(view) != 0)
            outRect.set(horizontalOffset, 0, horizontalOffset, verticalOffset)
        else outRect.set(horizontalOffset, verticalOffset, horizontalOffset, verticalOffset)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State){}
}