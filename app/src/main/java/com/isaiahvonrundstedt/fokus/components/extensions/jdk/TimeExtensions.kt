package com.isaiahvonrundstedt.fokus.components.extensions.jdk

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


fun LocalTime.print(pattern: String?): String? {
    return format(DateTimeFormatter.ofPattern(pattern))
}

fun LocalTime.toZonedDateTimeToday(): ZonedDateTime? {
    return LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        .with(this)
}

fun LocalTime.withCalendarFields(calendar: Calendar): LocalTime {
    return this.withHour(calendar.get(Calendar.HOUR_OF_DAY))
        .withMinute(calendar.get(Calendar.MINUTE))
        .withSecond(calendar.get(Calendar.SECOND))
}

/**
 *   An extension function used to
 *   determine if the ZonedDateTime object
 *   is same as the date today
 */
fun ZonedDateTime.isToday(): Boolean {
    return LocalDate.now().isEqual(this.toLocalDate())
}

/**
 *   An extension function used to
 *   determine if the ZonedDateTime object
 *   is the same as the next day
 */
fun ZonedDateTime.isTomorrow(): Boolean {
    return LocalDate.now().plusDays(1).compareTo(this.toLocalDate()) == 0
}

/**
 *   An extension function used to
 *   determine if the ZonedDateTime object
 *   is the same as the previous day
 */
fun ZonedDateTime.isYesterday(): Boolean {
    return LocalDate.now().minusDays(1).compareTo(this.toLocalDate()) == 0
}

/**
 *   An extension function used to determine
 *   if the ZonedDateTime object is before
 *   the current datetime
 */
fun ZonedDateTime.isBeforeNow(): Boolean {
    return this.isBefore(ZonedDateTime.now())
}

/**
 *   An extension function used to determine
 *   if the ZonedDateTime object is after
 *   the current datetime
 */
fun ZonedDateTime.isAfterNow(): Boolean {
    return this.isAfter(ZonedDateTime.now())
}

/**
 *  An extension function used to convert the
 *  current instance of ZonedDateTime to
 *  a legacy Calendar instance
 */
fun ZonedDateTime.toCalendar(): Calendar {
    return GregorianCalendar.from(this)
}

fun ZonedDateTime.print(pattern: String?): String? {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}