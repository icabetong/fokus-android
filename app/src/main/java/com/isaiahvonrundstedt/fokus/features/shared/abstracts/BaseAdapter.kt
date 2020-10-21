package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder?>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(callback){

    interface ActionListener {
        fun <T> onActionPerformed(t: T, action: Action, views: Map<String, View>)

        enum class Action { SELECT, DELETE }
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun <T> onBind(t: T)
    }
}