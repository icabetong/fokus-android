package com.isaiahvonrundstedt.fokus.features.shared.custom

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

class OffsetItemDecoration(@NonNull var context: Context,
                           @DimenRes var offset: Int): RecyclerView.ItemDecoration() {

    private var itemOffset = context.resources.getDimensionPixelOffset(offset)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) != 0)
            outRect.set(itemOffset, 0, itemOffset, itemOffset)
        else outRect.set(itemOffset, itemOffset, itemOffset, itemOffset)
    }

}