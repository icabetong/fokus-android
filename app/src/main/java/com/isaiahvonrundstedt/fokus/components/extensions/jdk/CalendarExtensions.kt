package com.isaiahvonrundstedt.fokus.components.extensions.jdk

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 *   An extension function used to convert
 *   the legacy Calendar instance to the
 *   modern java.time.LocalTime instance
 *
 *   @return the converted LocalTime instance
 */
fun Calendar.toLocalTime(): LocalTime {
    return LocalTime.of(
        this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE),
        this.get(Calendar.SECOND)
    )
}

/**
 *   An extension function used to convert
 *   the legacy Calendar instance to the
 *   modern java.time.ZonedDateTime instance
 *
 *   @return the converted ZonedDateTime instance
 */
fun Calendar.toZonedDateTime(): ZonedDateTime? {
    return ZonedDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
}