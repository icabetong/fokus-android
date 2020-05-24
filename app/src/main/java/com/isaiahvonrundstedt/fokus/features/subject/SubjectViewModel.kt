package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class SubjectViewModel(app: Application): BaseViewModel(app) {

    private var repository = SubjectRepository.getInstance(app)
    private var items: LiveData<List<Subject>>? = repository.fetch()

    fun fetch(): LiveData<List<Subject>>? = items

    fun insert(subject: Subject) = viewModelScope.launch {
        repository.insert(subject)
    }

    fun remove(subject: Subject) = viewModelScope.launch {
        repository.remove(subject)
    }

    fun update(subject: Subject) = viewModelScope.launch {
        repository.update(subject)
    }

}