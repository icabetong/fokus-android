package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel

class SubjectViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = SubjectRepository(app)
    private var items: LiveData<List<Subject>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Subject>>? = items

    fun insert(subject: Subject) {
        dataStore.insert(subject)
    }

    fun remove(subject: Subject) {
        dataStore.remove(subject)
    }

    fun update(subject: Subject) {
        dataStore.update(subject)
    }

}