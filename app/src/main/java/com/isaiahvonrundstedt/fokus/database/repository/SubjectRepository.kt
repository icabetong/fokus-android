package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubjectRepository(app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var subjects = database?.subject()

    fun fetch(): LiveData<List<Subject>>? = subjects?.fetch()

    fun search(query: String): LiveData<List<Subject>>? = subjects?.search(query)

    fun insert(subject: Subject) = GlobalScope.launch {
        subjects?.insert(subject)
    }

    fun remove(subject: Subject) = GlobalScope.launch {
        subjects?.remove(subject)
    }

    fun update(subject: Subject) = GlobalScope.launch {
        subjects?.update(subject)
    }



}