package com.isaiahvonrundstedt.fokus.features.event

import androidx.room.Embedded
import com.isaiahvonrundstedt.fokus.features.subject.Subject

/**
 *   Data class used for the presentation of
 *   events and subject in the UI
 */
data class EventResource @JvmOverloads constructor(
    @Embedded
    var event: Event,
    @Embedded
    var subject: Subject? = null
)