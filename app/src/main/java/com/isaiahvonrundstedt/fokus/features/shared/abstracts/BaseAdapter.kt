package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    abstract fun <T> insert(t: T)
    abstract fun <T> remove(t: T)
    abstract fun <T> update(t: T)

    interface ActionListener {
        fun <T> onActionPerformed(t: T, action: Action)

        enum class Action { SELECT, DELETE }
    }

    abstract class BaseViewHolder(itemView: View, protected var actionListener: ActionListener)
        : RecyclerView.ViewHolder(itemView) {

        abstract fun <T> onBind(t: T)
    }
}