package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH : RecyclerView.ViewHolder?>
    : RecyclerView.Adapter<VH>() {

    abstract fun onSwipe(position: Int, direction: Int)

    interface ActionListener {
        fun <T> onActionPerformed(t: T, action: Action)

        enum class Action { SELECT, MODIFY }
    }

    interface SwipeListener {
        fun <T> onSwipePerformed(position: Int, t: T, swipeDirection: Int)
    }
}