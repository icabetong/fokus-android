package com.isaiahvonrundstedt.fokus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.database.dao.*
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.ColorConverter
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.UriConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task

@Database(entities = [Subject::class, Task::class, Attachment::class,
    Notification::class, Event::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class, ColorConverter::class, UriConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun tasks(): TaskDAO
    abstract fun attachments(): AttachmentDAO
    abstract fun cores(): CoreDAO
    abstract fun subjects(): SubjectDAO
    abstract fun notifications(): NotificationDAO
    abstract fun events(): EventDAO

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "fokus").build()
                }
            }
            return instance
        }

        fun close() {
            instance = null
        }
    }

}