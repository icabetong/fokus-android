package com.isaiahvonrundstedt.fokus.features.archived

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

abstract class ArchivedAdapter<T: Parcelable, VH: BaseAdapter.BaseViewHolder>(callback: DiffUtil.ItemCallback<T>)
    : BaseAdapter<T, VH>(callback) {

        interface ArchivedItemClickListener {
            fun <T> onArchivedItemClicked(data: T)
        }
    }