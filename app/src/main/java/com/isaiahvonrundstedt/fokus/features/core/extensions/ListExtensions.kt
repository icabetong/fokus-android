package com.isaiahvonrundstedt.fokus.features.core.extensions

import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task

/**
 *  Extension function a generic list that has types used by
 *  the application that will get the item using the items
 *  id from the list
 */
fun <T> List<T>.getUsingID(id: String): T? {
    this.forEach {
        if (it is Task)
            if (it.taskID == id) return it

        if (it is Event)
            if (it.id == id) return it

        if (it is Subject)
            if (it.id == id) return it

        if (it is Attachment)
            if (it.id == id) return it

        if (it is Notification)
            if (it.id == id) return it
    }
    return null
}

/**
 *  Extension function to create an ArrayList
 *  from the current List object
 */
fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}