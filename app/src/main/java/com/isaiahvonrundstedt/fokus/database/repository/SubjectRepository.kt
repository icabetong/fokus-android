package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.coroutines.Job

class SubjectRepository private constructor (app: Application) {

    companion object {
        private var instance: SubjectRepository? = null

        fun getInstance(app: Application): SubjectRepository {
            if (instance == null) {
                synchronized(SubjectRepository::class) {
                    instance = SubjectRepository(app)
                }
            }
            return instance!!
        }
    }

    private var database = AppDatabase.getInstance(app)
    private var subjects = database?.subjects()

    fun fetch(): LiveData<List<Subject>>? = subjects?.fetch()

    fun search(query: String): LiveData<List<Subject>>? = subjects?.search(query)

    suspend fun insert(subject: Subject) {
        subjects?.insert(subject)
    }

    suspend fun remove(subject: Subject) {
        subjects?.remove(subject)
    }

    suspend fun update(subject: Subject) {
        subjects?.update(subject)
    }



}