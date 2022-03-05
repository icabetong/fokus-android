package com.isaiahvonrundstedt.fokus.components.modules

import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.dao.*
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDAO = database.tasks()

    @Provides
    fun provideAttachmentDao(database: AppDatabase): AttachmentDAO = database.attachments()

    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDAO = database.subjects()

    @Provides
    fun provideScheduleDao(database: AppDatabase): ScheduleDAO = database.schedules()

    @Provides
    fun provideEventDao(database: AppDatabase): EventDAO = database.events()

    @Provides
    fun provideLogDao(database: AppDatabase): LogDAO = database.logs()

    @Provides
    fun provideTaskRepository(
        @ApplicationContext
        context: Context,
        taskDao: TaskDAO,
        attachmentDao: AttachmentDAO,
        preferenceManager: PreferenceManager,
        workManager: WorkManager,
        notificationManager: NotificationManager
    ): TaskRepository {
        return TaskRepository(
            context,
            taskDao,
            attachmentDao,
            preferenceManager,
            workManager,
            notificationManager
        )
    }

    @Provides
    fun provideSubjectRepository(
        @ApplicationContext
        context: Context,
        subjectDAO: SubjectDAO,
        scheduleDAO: ScheduleDAO,
        preferenceManager: PreferenceManager,
        workManager: WorkManager
    ): SubjectRepository {
        return SubjectRepository(context, subjectDAO, scheduleDAO, preferenceManager, workManager)
    }

    @Provides
    fun provideEventRepository(
        @ApplicationContext
        context: Context,
        eventDAO: EventDAO,
        preferenceManager: PreferenceManager,
        workManager: WorkManager,
        notificationManager: NotificationManager
    ): EventRepository {
        return EventRepository(
            context,
            eventDAO,
            preferenceManager,
            workManager,
            notificationManager
        )
    }

    @Provides
    fun provideLogRepository(dao: LogDAO): LogRepository = LogRepository(dao)
}