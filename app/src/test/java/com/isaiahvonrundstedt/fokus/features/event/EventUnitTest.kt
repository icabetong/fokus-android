package com.isaiahvonrundstedt.fokus.features.event

import org.junit.Test
import java.time.ZonedDateTime

class EventUnitTest {

    @Test
    fun `should return true if schedule is today`() {
        val event = Event(schedule = ZonedDateTime.now())

        assert(event.isToday())
    }
}