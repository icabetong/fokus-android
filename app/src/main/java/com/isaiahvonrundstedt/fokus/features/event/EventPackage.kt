package com.isaiahvonrundstedt.fokus.features.event

import android.os.Parcelable
import androidx.room.Embedded
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize

/**
 *   Data class used for the presentation of
 *   events and subject in the UI
 */
@Parcelize
data class EventPackage @JvmOverloads constructor(
    @Embedded
    var event: Event,
    @Embedded
    var subject: Subject? = null
): Parcelable