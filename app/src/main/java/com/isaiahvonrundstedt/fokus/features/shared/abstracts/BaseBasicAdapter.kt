package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseBasicAdapter<T, VH : BaseBasicAdapter.BaseBasicViewHolder<T>>
    : RecyclerView.Adapter<VH>() {

    protected val items = arrayListOf<T>()

    fun submitList(list: List<T>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    interface ActionListener<T> {
        fun onActionPerformed(t: T, position: Int, action: Action)

        enum class Action { SELECT, DELETE }
    }

    abstract class BaseBasicViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBind(t: T, position: Int)
    }
}