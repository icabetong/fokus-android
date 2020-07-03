package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class SubjectViewModel(app: Application): BaseViewModel(app) {

    private var repository = SubjectRepository.getInstance(app)
    private var items: LiveData<List<SubjectResource>>? = repository.fetch()

    fun fetch(): LiveData<List<SubjectResource>>? = items

    fun insert(subject: Subject, scheduleList: List<Schedule>) = viewModelScope.launch {
        repository.insert(subject, scheduleList)
    }

    fun remove(subject: Subject) = viewModelScope.launch {
        repository.remove(subject)
    }

    fun update(subject: Subject, scheduleList: List<Schedule> = emptyList()) = viewModelScope.launch {
        repository.update(subject, scheduleList)
    }

}