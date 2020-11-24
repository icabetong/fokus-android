package com.isaiahvonrundstedt.fokus.components.modules

import android.content.Context
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.dao.*
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
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
    fun provideTaskRepository(taskDao: TaskDAO, attachmentDao: AttachmentDAO): TaskRepository {
        return TaskRepository(taskDao, attachmentDao)
    }

    @Provides
    fun provideSubjectRepository(subjectDAO: SubjectDAO, scheduleDAO: ScheduleDAO): SubjectRepository {
        return SubjectRepository(subjectDAO, scheduleDAO)
    }

    @Provides
    fun provideEventRepository(dao: EventDAO): EventRepository = EventRepository(dao)
    @Provides
    fun provideLogRepository(dao: LogDAO): LogRepository = LogRepository(dao)
}