package com.isaiahvonrundstedt.fokus.features.shared.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemMenuBinding

class MenuAdapter(
    activity: Activity?,
    @MenuRes
    private val resId: Int,
    private val menuItemListener: MenuItemListener
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private var itemList = mutableListOf<MenuItem>()

    init {
        val temp = PopupMenu(activity, null).menu
        activity?.menuInflater?.inflate(resId, temp)

        for (i in 0 until temp.size()) {
            val item = temp.getItem(i)
            itemList.add(MenuItem(item.itemId, item.icon, item.title.toString()))
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = LayoutItemMenuBinding.bind(itemView)

        fun onBind(item: MenuItem) {
            binding.iconView.setImageDrawable(item.icon)
            binding.titleView.text = item.title
            binding.root.setOnClickListener { menuItemListener.onItemSelected(item.id) }
        }
    }

    data class MenuItem(var id: Int, var icon: Drawable?, var title: String)

    interface MenuItemListener {
        fun onItemSelected(id: Int)
    }
}