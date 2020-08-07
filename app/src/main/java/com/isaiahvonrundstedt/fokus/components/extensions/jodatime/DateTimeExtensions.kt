package com.isaiahvonrundstedt.fokus.components.extensions.jodatime

import org.joda.time.DateTime
import org.joda.time.LocalDate

fun DateTime.isToday(): Boolean {
    return this.toLocalDate().isEqual(LocalDate.now())
}

fun DateTime.isTomorrow(): Boolean {
    return LocalDate.now().plusDays(1).compareTo(this.toLocalDate()) == 0
}

fun DateTime.isYesterday(): Boolean {
    return LocalDate.now().minusDays(1).compareTo(this.toLocalDate()) == 0
}