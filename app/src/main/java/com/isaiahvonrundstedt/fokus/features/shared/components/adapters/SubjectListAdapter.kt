package com.isaiahvonrundstedt.fokus.features.shared.components.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.subject.Subject

class SubjectListAdapter(private val listener: ItemSelected):
    RecyclerView.Adapter<SubjectListAdapter.ViewHolder>() {

    private var itemList = ArrayList<Subject>()

    fun setObservableItems(items: List<Subject>) {
        val result = DiffUtil.calculateDiff(DiffCallback(itemList, items))

        itemList.clear()
        itemList.addAll(items)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_subject,
            parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    interface ItemSelected {
        fun onItemSelected(subject: Subject)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView.findViewById(R.id.rootView)
        private val tagView: AppCompatImageView = itemView.findViewById(R.id.tagView)
        private val titleView: AppCompatTextView = itemView.findViewById(R.id.titleView)
        private val summaryView: AppCompatTextView = itemView.findViewById(R.id.summaryView)

        fun onBind(s: Subject) {
            rootView.setOnClickListener { listener.onItemSelected(s) }
            titleView.text = s.code
            summaryView.text = s.description

            tagView.setImageDrawable(s.tintDrawable(tagView.drawable))
        }
    }

    class DiffCallback(private var oldItems: List<Subject>,
                           private var newItems: List<Subject>): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size
    }
}