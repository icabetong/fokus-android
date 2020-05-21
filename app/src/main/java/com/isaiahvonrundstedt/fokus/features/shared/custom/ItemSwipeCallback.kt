package com.isaiahvonrundstedt.fokus.features.shared.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.archived.ArchivedAdapter
import com.isaiahvonrundstedt.fokus.features.event.EventAdapter
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectAdapter
import com.isaiahvonrundstedt.fokus.features.task.TaskAdapter

class ItemSwipeCallback<T, VH: RecyclerView.ViewHolder>(context: Context, private var adapter: BaseAdapter<T, VH>)
    : ItemTouchHelper.Callback() {

    private var iconEnd: Drawable = ContextCompat.getDrawable(context, R.drawable.shape_swipe_end)!!
    private var iconStart: Drawable = ContextCompat.getDrawable(context, R.drawable.shape_swipe_start)!!

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val movementFlags =
            if (adapter is TaskAdapter || adapter is ArchivedAdapter)
                ItemTouchHelper.START or ItemTouchHelper.END
            else if (adapter is NotificationAdapter || adapter is SubjectAdapter || adapter is EventAdapter)
                ItemTouchHelper.START
            else 0

        return makeMovementFlags(0, movementFlags)
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

        val itemView: View = viewHolder.itemView

        if (dX > 0) {
            val iconMargin = (itemView.height - iconStart.intrinsicHeight) / 2
            val iconTop = itemView.top + (itemView.height - iconStart.intrinsicHeight) / 2
            val iconBottom = iconTop + iconStart.intrinsicHeight

            val iconLeft = itemView.left + iconMargin
            val iconRight = iconLeft + iconStart.intrinsicWidth

            iconStart.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            iconStart.draw(c)
        } else if (dX < 0) {
            val iconMargin = (itemView.height - iconEnd.intrinsicHeight) / 2
            val iconTop = itemView.top + (itemView.height - iconEnd.intrinsicHeight) / 2
            val iconBottom = iconTop + iconEnd.intrinsicHeight

            val iconLeft: Int = itemView.right - iconMargin - iconEnd.intrinsicWidth
            val iconRight: Int = itemView.right - iconMargin

            iconEnd.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            iconEnd.draw(c)
        }
    }
}