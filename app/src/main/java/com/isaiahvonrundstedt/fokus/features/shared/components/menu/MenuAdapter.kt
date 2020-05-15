package com.isaiahvonrundstedt.fokus.features.shared.components.menu

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R

class MenuAdapter(private var activity: Activity?,
                  private var listener: MenuItemSelectionListener):
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var itemList = ArrayList<MenuItem>()

    interface MenuItemSelectionListener {
        fun onItemSelected(id: Int)
    }

    fun setItems(@MenuRes menu: Int) {
        val items = ArrayList<MenuItem>()
        val tempMenu = PopupMenu(activity, null).menu
        activity?.menuInflater?.inflate(menu, tempMenu)

        for (i in 0 until tempMenu.size()) {
            val item = tempMenu.getItem(i)
            val menuItem = MenuItem().apply {
                id = item.itemId
                title = item.title.toString()
                icon = item.icon
            }
            items.add(menuItem)
        }

        val callback = DiffCallback(itemList, items)
        val result = DiffUtil.calculateDiff(callback)

        itemList.clear()
        itemList.addAll(items)
        result.dispatchUpdatesTo(this)
    }

    inner class MenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val iconView: AppCompatImageView = itemView.findViewById(R.id.iconView)
        private val titleView: AppCompatTextView = itemView.findViewById(R.id.titleView)

        fun onBind(menuItem: MenuItem) {
            iconView.setImageDrawable(menuItem.icon)
            titleView.text = menuItem.title
            rootView.setOnClickListener { listener.onItemSelected(menuItem.id) }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MenuViewHolder {
        val rowView: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_item_menu,
            viewGroup, false)
        return MenuViewHolder(rowView)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    private class DiffCallback (private var oldItems: List<MenuItem>,
                                private var newItems: List<MenuItem>): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = oldItems[oldItemPosition] == newItems[newItemPosition]

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldItems[oldItemPosition] == newItems[newItemPosition]

    }
}