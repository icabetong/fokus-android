package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder?>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(callback) {

    abstract fun onSwipe(position: Int, direction: Int)

    interface ActionListener {
        fun <T> onActionPerformed(t: T, action: Action)

        enum class Action { SELECT, MODIFY }
    }

    interface SwipeListener {
        fun <T> onSwipePerformed(position: Int, t: T, swipeDirection: Int)
    }
}