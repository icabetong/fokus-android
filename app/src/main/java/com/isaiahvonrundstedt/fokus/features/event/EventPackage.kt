package com.isaiahvonrundstedt.fokus.features.event

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize

/**
 *   Data class used for the presentation of
 *   events and subject in the UI
 */
@Parcelize
data class EventPackage(
    @Embedded
    var event: Event,
    @Embedded
    var subject: Subject? = null
) : Parcelable {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EventPackage>() {
            override fun areItemsTheSame(oldItem: EventPackage, newItem: EventPackage): Boolean {
                return oldItem.event.eventID == newItem.event.eventID
            }

            override fun areContentsTheSame(oldItem: EventPackage, newItem: EventPackage): Boolean {
                return oldItem == newItem
            }
        }
    }
}