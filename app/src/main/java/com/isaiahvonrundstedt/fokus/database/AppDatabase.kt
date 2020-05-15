package com.isaiahvonrundstedt.fokus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.database.dao.*
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.notifications.Notification
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.*
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task

@Database(entities = [Subject::class, Task::class, Attachment::class, Notification::class], version = 1)
@TypeConverters(DateTimeConverter::class, ColorConverter::class, UriConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun tasks(): TaskDAO
    abstract fun attachment(): AttachmentDAO
    abstract fun bundle(): CoreDAO
    abstract fun subject(): SubjectDAO
    abstract fun notification(): NotificationDAO

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
    }

}