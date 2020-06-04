package com.isaiahvonrundstedt.fokus.features.event

import androidx.room.Embedded
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.subject.Subject

data class EventResource @JvmOverloads constructor (
    @Embedded
    var event: Event,
    @Embedded
    var subject: Subject? = null
)