package com.isaiahvonrundstedt.fokus.components.extensions.jdk

import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task

fun <T> List<T>.getIndexByID(id: String): Int {
    this.forEachIndexed { index, it ->
        if (it is Task)
            if (it.taskID == id) return index

        if (it is Event)
            if (it.eventID == id) return index

        if (it is Subject)
            if (it.subjectID == id) return index

        if (it is Attachment)
            if (it.attachmentID == id) return index

        if (it is Log)
            if (it.logID == id) return index

        if (it is Schedule)
            if (it.scheduleID == id) return index
    }
    return -1
}

/**
 *  Extension function to create an ArrayList
 *  from the current List object
 */
fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}