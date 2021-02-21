package com.isaiahvonrundstedt.fokus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.isaiahvonrundstedt.fokus.database.converter.ColorConverter
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.database.dao.*
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task

@Database(entities = [Subject::class, Task::class, Attachment::class, Log::class,
    Event::class, Schedule::class],
    version = AppDatabase.DATABASE_VERSION, exportSchema = false)
@TypeConverters(DateTimeConverter::class, ColorConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjects(): SubjectDAO
    abstract fun schedules(): ScheduleDAO
    abstract fun tasks(): TaskDAO
    abstract fun attachments(): AttachmentDAO
    abstract fun events(): EventDAO
    abstract fun logs(): LogDAO

    companion object {
        const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "fokus"

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, DATABASE_NAME)
                        .addMigrations(*migrations)
                        .build()
                }
            }
            return instance!!
        }

        private var migration_4_6 = object: Migration(4, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // schedules
                    execSQL("CREATE TABLE IF NOT EXISTS `schedules_new` (`scheduleID` TEXT NOT NULL, `daysOfWeek` INTEGER NOT NULL, `weeksOfMonth` INTEGER NOT NULL DEFAULT 15,`startTime` TEXT, `endTime` TEXT, `subject` TEXT, PRIMARY KEY(`scheduleID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("ALTER TABLE schedules RENAME TO schedules_old")
                    execSQL("ALTER TABLE schedules_new RENAME TO schedules")
                    execSQL("INSERT INTO schedules (`scheduleID`, `daysOfWeek`, `startTime`, `endTime`, `subject`) SELECT * FROM `schedules_old`")
                    execSQL("DROP TABLE schedules_old")

                    execSQL("ALTER TABLE tasks ADD COLUMN `isTaskArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN `isEventArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE subjects ADD COLUMN `isSubjectArchived` INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        private var migration_5_6 = object: Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE tasks ADD COLUMN `isTaskArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN `isEventArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE subjects ADD COLUMN `isSubjectArchived` INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        private val migrations = arrayOf(migration_4_6, migration_5_6)
    }
}