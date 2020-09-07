package com.isaiahvonrundstedt.fokus.components.custom

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Swipeable

class ItemSwipeCallback<T : Swipeable>(context: Context, private var adapter: T)
    : ItemTouchHelper.Callback() {

    private var isThemeDark: Boolean = false
    private var icon = ContextCompat.getDrawable(context, R.drawable.ic_hero_trash_24)
    private var background: Drawable

    init {
        isThemeDark = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val color = if (isThemeDark)
            COLOR_BACKGROUND_DARK
        else COLOR_BACKGROUND_LIGHT

        background = ColorDrawable(Color.parseColor(color))
    }

    override fun getMovementFlags(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.START)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onSwipe(viewHolder.adapterPosition, direction)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView: View = viewHolder.itemView
        val backgroundCornerOffset = 40

        with(background) {
            setBounds(itemView.right + dX.toInt() - backgroundCornerOffset, itemView.top,
                itemView.right, itemView.bottom)
            draw(c)
        }

        icon?.let {
            val tintColor = if (isThemeDark)
                Color.parseColor(COLOR_ICON_DARK)
            else Color.parseColor(COLOR_ICON_LIGHT)

            it.setTint(tintColor)

            val iconMargin: Int = (itemView.height - it.intrinsicHeight) / 2

            val iconTop: Int = itemView.top + (itemView.height - it.intrinsicHeight) / 2
            val iconBottom: Int = iconTop + it.intrinsicHeight
            val iconLeft: Int = itemView.right - iconMargin - it.intrinsicWidth
            val iconRight: Int = itemView.right - iconMargin

            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            it.draw(c)
        }
    }

    companion object {
        const val COLOR_ICON_LIGHT = "#ea4335"
        const val COLOR_ICON_DARK = "#000000"

        const val COLOR_BACKGROUND_LIGHT = "#66ea4335"
        const val COLOR_BACKGROUND_DARK = "#ea4335"
    }
}