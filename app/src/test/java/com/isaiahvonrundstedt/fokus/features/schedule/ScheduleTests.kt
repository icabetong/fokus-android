package com.isaiahvonrundstedt.fokus.features.schedule

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class ScheduleTests {

    @Test
    fun `Check schedule if it's Monday Wednesday and Thursday`() {
        val daysOfWeek = Schedule.BIT_VALUE_MONDAY +
                Schedule.BIT_VALUE_WEDNESDAY + Schedule.BIT_VALUE_THURSDAY

        assertTrue(Schedule.parseDaysOfWeek(daysOfWeek).contains(DayOfWeek.MONDAY.value))
        assertTrue(Schedule.parseDaysOfWeek(daysOfWeek).contains(DayOfWeek.WEDNESDAY.value))
        assertTrue(Schedule.parseDaysOfWeek(daysOfWeek).contains(DayOfWeek.THURSDAY.value))
    }

}