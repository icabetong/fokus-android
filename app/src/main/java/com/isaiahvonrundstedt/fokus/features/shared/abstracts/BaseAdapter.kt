package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder?>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(callback) {

    abstract fun onSwipe(position: Int, direction: Int, itemView: View)

    interface ActionListener {
        fun <T> onActionPerformed(t: T, action: Action, itemView: View)

        enum class Action { SELECT, MODIFY, DELETE }
    }
}