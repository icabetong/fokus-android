package com.isaiahvonrundstedt.fokus.features.shared.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R

class NavigationAdapter(private val activity: Activity?,
                        private val navigationListener: NavigationListener)
    : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {

    private var itemList = mutableListOf<NavigationItem>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val iconView: ImageView = itemView.findViewById(R.id.iconView)
        private val titleView: TextView = itemView.findViewById(R.id.titleView)

        fun onBind(item: NavigationItem) {
            iconView.setImageDrawable(item.icon)
            titleView.text = item.title
            rootView.setOnClickListener { navigationListener.onNavigate(item.id) }
        }
    }

    data class NavigationItem(var id: Int, var icon: Drawable?, var title: String)

    interface NavigationListener {
        fun onNavigate(id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_menu,
            parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(itemList[holder.adapterPosition])
    }

    fun setItems(@MenuRes id: Int) {
        val temp = PopupMenu(activity, null).menu
        activity?.menuInflater?.inflate(id, temp)

        for (i in 0 until temp.size()) {
            val item = temp.getItem(i)
            itemList.add(NavigationItem(item.itemId, item.icon, item.title.toString()))
        }
        notifyDataSetChanged()
    }

}