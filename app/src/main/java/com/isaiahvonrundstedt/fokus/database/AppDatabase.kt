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
import java.time.ZonedDateTime

@Database(
    entities = [Subject::class, Task::class, Attachment::class, Log::class, Event::class, Schedule::class],
    version = AppDatabase.DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(DateTimeConverter::class, ColorConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjects(): SubjectDAO
    abstract fun schedules(): ScheduleDAO
    abstract fun tasks(): TaskDAO
    abstract fun attachments(): AttachmentDAO
    abstract fun events(): EventDAO
    abstract fun logs(): LogDAO

    companion object {
        const val DATABASE_VERSION = 8
        private const val DATABASE_NAME = "fokus"

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, DATABASE_NAME
                    )
                        .addMigrations(*migrations)
                        .build()
                }
            }
            return instance!!
        }

        private var migration_4_6 = object : Migration(4, 6) {
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

        private var migration_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE tasks ADD COLUMN `isTaskArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN `isEventArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE subjects ADD COLUMN `isSubjectArchived` INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        private var migration_4_7 = object : Migration(4, 7) {
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

                var cursor = database.query("SELECT * FROM tasks")
                cursor.moveToFirst()

                val taskList = arrayListOf<Task>()
                while (cursor.moveToNext()) {
                    val task = Task()
                    task.taskID = cursor.getString(cursor.getColumnIndex("taskID"))
                    task.name = cursor.getString(cursor.getColumnIndex("name"))
                    task.notes = cursor.getString(cursor.getColumnIndex("notes"))
                    task.subject = cursor.getString(cursor.getColumnIndex("subject"))
                    task.isFinished = cursor.getInt(cursor.getColumnIndex("isFinished")) > 0
                    task.isImportant = cursor.getInt(cursor.getColumnIndex("isImportant")) > 0
                    task.isTaskArchived = cursor.getInt(cursor.getColumnIndex("isTaskArchived")) > 0
                    task.dueDate =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dueDate")))
                    task.dateAdded =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dateAdded")))
                            ?: ZonedDateTime.now()

                    taskList.add(task)
                }
                cursor.close()

                database.execSQL("DELETE FROM tasks")
                taskList.forEach {
                    val statement =
                        database.compileStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    with(statement) {
                        bindString(1, it.taskID)
                        bindString(2, it.name)
                        bindString(3, it.notes)
                        bindString(4, it.subject)
                        bindLong(5, if (it.isImportant) 1 else 0)
                        bindString(6, DateTimeConverter.fromZonedDateTime(it.dateAdded))
                        bindString(7, DateTimeConverter.fromZonedDateTime(it.dueDate))
                        bindLong(8, if (it.isFinished) 1 else 0)
                        bindLong(9, if (it.isTaskArchived) 1 else 0)
                    }
                    statement.executeInsert()
                }
                taskList.clear()

                cursor = database.query("SELECT * FROM events")
                cursor.moveToFirst()

                val eventList = arrayListOf<Event>()
                while (cursor.moveToNext()) {
                    val event = Event()
                    event.eventID = cursor.getString(cursor.getColumnIndex("eventID"))
                    event.name = cursor.getString(cursor.getColumnIndex("name"))
                    event.notes = cursor.getString(cursor.getColumnIndex("notes"))
                    event.schedule =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("schedule")))
                    event.location = cursor.getString(cursor.getColumnIndex("location"))
                    event.isImportant = cursor.getInt(cursor.getColumnIndex("isImportant")) > 0
                    event.isEventArchived =
                        cursor.getInt(cursor.getColumnIndex("isEventArchived")) > 0
                    event.dateAdded =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dateAdded")))
                            ?: ZonedDateTime.now()
                }
                cursor.close()

                database.execSQL("DELETE FROM events")
                eventList.forEach {
                    val statement =
                        database.compileStatement("INSERT INTO events VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    with(statement) {
                        bindString(1, it.eventID)
                        bindString(2, it.name)
                        bindString(3, it.location)
                        bindString(4, it.subject)
                        bindLong(5, if (it.isImportant) 1 else 0)
                        bindLong(6, if (it.isEventArchived) 1 else 0)
                        bindString(7, DateTimeConverter.fromZonedDateTime(it.schedule))
                        bindString(8, DateTimeConverter.fromZonedDateTime(it.dateAdded))

                    }
                    statement.executeInsert()
                }
                eventList.clear()
            }
        }

        private var migration_5_7 = object : Migration(5, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE tasks ADD COLUMN `isTaskArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN `isEventArchived` INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE subjects ADD COLUMN `isSubjectArchived` INTEGER NOT NULL DEFAULT 0")

                    execSQL("CREATE TABLE IF NOT EXISTS `events_new` (`eventID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `location` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `isEventArchived` INTEGER NOT NULL, `schedule` TEXT, `dateAdded` TEXT, PRIMARY KEY(`eventID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO `events_new` SELECT * FROM `events`")
                    execSQL("DROP TABLE `events`")
                    execSQL("ALTER TABLE `events_new` RENAME TO `events`")
                }

                var cursor = database.query("SELECT * FROM tasks")
                cursor.moveToFirst()

                val taskList = arrayListOf<Task>()
                while (cursor.moveToNext()) {
                    val task = Task()
                    task.taskID = cursor.getString(cursor.getColumnIndex("taskID"))
                    task.name = cursor.getString(cursor.getColumnIndex("name"))
                    task.notes = cursor.getString(cursor.getColumnIndex("notes"))
                    task.subject = cursor.getString(cursor.getColumnIndex("subject"))
                    task.isFinished = cursor.getInt(cursor.getColumnIndex("isFinished")) > 0
                    task.isImportant = cursor.getInt(cursor.getColumnIndex("isImportant")) > 0
                    task.isTaskArchived = cursor.getInt(cursor.getColumnIndex("isTaskArchived")) > 0
                    task.dueDate =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dueDate")))
                    task.dateAdded =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dateAdded")))
                            ?: ZonedDateTime.now()

                    taskList.add(task)
                }
                cursor.close()

                database.execSQL("DELETE FROM tasks")
                taskList.forEach {
                    val statement =
                        database.compileStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    with(statement) {
                        bindString(1, it.taskID)
                        bindString(2, it.name)
                        bindString(3, it.notes)
                        bindString(4, it.subject)
                        bindLong(5, if (it.isImportant) 1 else 0)
                        bindString(6, DateTimeConverter.fromZonedDateTime(it.dateAdded))
                        bindString(7, DateTimeConverter.fromZonedDateTime(it.dueDate))
                        bindLong(8, if (it.isFinished) 1 else 0)
                        bindLong(9, if (it.isTaskArchived) 1 else 0)
                    }
                    statement.executeInsert()
                }
                taskList.clear()

                cursor = database.query("SELECT * FROM events")
                cursor.moveToFirst()

                val eventList = arrayListOf<Event>()
                while (cursor.moveToNext()) {
                    val event = Event()
                    event.eventID = cursor.getString(cursor.getColumnIndex("eventID"))
                    event.name = cursor.getString(cursor.getColumnIndex("name"))
                    event.notes = cursor.getString(cursor.getColumnIndex("notes"))
                    event.schedule =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("schedule")))
                    event.location = cursor.getString(cursor.getColumnIndex("location"))
                    event.isImportant = cursor.getInt(cursor.getColumnIndex("isImportant")) > 0
                    event.isEventArchived =
                        cursor.getInt(cursor.getColumnIndex("isEventArchived")) > 0
                    event.dateAdded =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dateAdded")))
                            ?: ZonedDateTime.now()
                }
                cursor.close()

                database.execSQL("DELETE FROM events")
                eventList.forEach {
                    val statement =
                        database.compileStatement("INSERT INTO events VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    with(statement) {
                        bindString(1, it.eventID)
                        bindString(2, it.name)
                        bindString(3, it.notes)
                        bindString(4, it.location)
                        bindString(5, it.subject)
                        bindLong(6, if (it.isImportant) 1 else 0)
                        bindLong(7, if (it.isEventArchived) 1 else 0)
                        bindString(8, DateTimeConverter.fromZonedDateTime(it.schedule))
                        bindString(9, DateTimeConverter.fromZonedDateTime(it.dateAdded))
                    }
                    statement.executeInsert()
                }
                eventList.clear()
            }
        }

        private var migration_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("CREATE TABLE IF NOT EXISTS `events_new` (`eventID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `location` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `isEventArchived` INTEGER NOT NULL, `schedule` TEXT, `dateAdded` TEXT, PRIMARY KEY(`eventID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO `events_new` SELECT * FROM `events`")
                    execSQL("DROP TABLE `events`")
                    execSQL("ALTER TABLE `events_new` RENAME TO `events`")
                }

                var cursor = database.query("SELECT * FROM tasks")
                cursor.moveToFirst()

                val taskList = arrayListOf<Task>()
                while (cursor.moveToNext()) {
                    val task = Task()
                    task.taskID = cursor.getString(cursor.getColumnIndex("taskID"))
                    task.name = cursor.getString(cursor.getColumnIndex("name"))
                    task.notes = cursor.getString(cursor.getColumnIndex("notes"))
                    task.subject = cursor.getString(cursor.getColumnIndex("subject"))
                    task.isFinished = cursor.getInt(cursor.getColumnIndex("isFinished")) > 0
                    task.isImportant = cursor.getInt(cursor.getColumnIndex("isImportant")) > 0
                    task.isTaskArchived = cursor.getInt(cursor.getColumnIndex("isTaskArchived")) > 0
                    task.dueDate =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dueDate")))
                    task.dateAdded =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dateAdded")))
                            ?: ZonedDateTime.now()

                    taskList.add(task)
                }
                cursor.close()

                database.execSQL("DELETE FROM tasks")
                taskList.forEach {
                    val statement =
                        database.compileStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    with(statement) {
                        bindString(1, it.taskID)
                        bindString(2, it.name)
                        bindString(3, it.notes)
                        bindString(4, it.subject ?: "")
                        bindLong(5, if (it.isImportant) 1 else 0)
                        bindString(6, DateTimeConverter.fromZonedDateTime(it.dateAdded))
                        bindString(7, DateTimeConverter.fromZonedDateTime(it.dueDate))
                        bindLong(8, if (it.isFinished) 1 else 0)
                        bindLong(9, if (it.isTaskArchived) 1 else 0)
                    }
                    statement.executeInsert()
                }
                taskList.clear()

                cursor = database.query("SELECT * FROM events")
                cursor.moveToFirst()

                val eventList = arrayListOf<Event>()
                while (cursor.moveToNext()) {
                    val event = Event()
                    event.eventID = cursor.getString(cursor.getColumnIndex("eventID"))
                    event.name = cursor.getString(cursor.getColumnIndex("name"))
                    event.notes = cursor.getString(cursor.getColumnIndex("notes"))
                    event.schedule =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("schedule")))
                    event.location = cursor.getString(cursor.getColumnIndex("location"))
                    event.isImportant = cursor.getInt(cursor.getColumnIndex("isImportant")) > 0
                    event.isEventArchived =
                        cursor.getInt(cursor.getColumnIndex("isEventArchived")) > 0
                    event.dateAdded =
                        DateTimeConverter.toZonedDateTime(cursor.getString(cursor.getColumnIndex("dateAdded")))
                            ?: ZonedDateTime.now()
                    event.subject = cursor.getString(cursor.getColumnIndex("subject"))
                }
                cursor.close()

                database.execSQL("DELETE FROM events")
                eventList.forEach {
                    val statement =
                        database.compileStatement("INSERT INTO events VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    with(statement) {
                        bindString(1, it.eventID)
                        bindString(2, it.name)
                        bindString(3, it.notes)
                        bindString(4, it.location)
                        bindString(5, it.subject ?: "")
                        bindLong(6, if (it.isImportant) 1 else 0)
                        bindLong(7, if (it.isEventArchived) 1 else 0)
                        bindString(8, DateTimeConverter.fromZonedDateTime(it.schedule))
                        bindString(9, DateTimeConverter.fromZonedDateTime(it.dateAdded))
                    }
                    statement.executeInsert()
                }
                eventList.clear()
            }
        }

        private var migration_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE subjects ADD COLUMN `instructor` TEXT")
                    execSQL("ALTER TABLE schedules ADD COLUMN `classroom` TEXT")
                }
            }
        }

        private val migrations =
            arrayOf(migration_4_6, migration_5_6, migration_4_7, migration_5_7, migration_6_7, migration_7_8)
    }
}