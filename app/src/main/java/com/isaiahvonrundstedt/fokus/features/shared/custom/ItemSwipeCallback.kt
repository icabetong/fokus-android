package com.isaiahvonrundstedt.fokus.features.shared.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class ItemSwipeCallback<T, VH: RecyclerView.ViewHolder>(context: Context, private var adapter: BaseAdapter<T, VH>)
    : ItemTouchHelper.Callback() {

    private var icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.shape_item_swipe_delete)

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.START)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onSwipe(viewHolder.adapterPosition, direction)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.5f
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (icon != null && dX < 0) {
            val itemView: View = viewHolder.itemView

            val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
            val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
            val iconBottom = iconTop + icon!!.intrinsicHeight

            val iconLeft: Int = itemView.right - iconMargin - icon!!.intrinsicWidth
            val iconRight: Int = itemView.right - iconMargin

            icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            icon!!.draw(c)
        }
    }
}