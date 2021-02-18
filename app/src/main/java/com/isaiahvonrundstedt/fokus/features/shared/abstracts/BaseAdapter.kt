package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder?>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(callback){

    /**
     * interface used when triggering
     * an archive swipe in adapter.
     */
    interface ArchiveListener {
        /**
         * @param t the item that will be archived.
         */
        fun <T> onItemArchive(t: T)
    }

    /**
     * interface used when the adapter needs
     * both SELECT and DELETE actions into one
     * unified listener
     */
    interface ActionListener {
        /**
         * @param t the data that will be passed by the adapter to the view
         *
         * @param action the action triggered by the user, e.g. SELECT or DELETE
         *
         * @param container the root view of the item view that is needed to perform
         * transitions
         */
        fun <T> onActionPerformed(t: T, action: Action, container: View?)

        /**
         * Actions used by the listener
         */
        enum class Action { SELECT, DELETE }
    }

    /**
     * interface used when the adapter only needs
     * the SELECT action
     */
    interface SelectListener {
        /**
         * @param t the data that will be passed to the view or presenter
         */
        fun <T> onItemSelected(t: T)
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun <T> onBind(t: T)
    }
}