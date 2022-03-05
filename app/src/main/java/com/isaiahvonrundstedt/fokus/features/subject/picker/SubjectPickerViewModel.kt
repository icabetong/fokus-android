package com.isaiahvonrundstedt.fokus.features.subject.picker

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectPickerViewModel @Inject constructor(
    private val repository: SubjectRepository,
) : ViewModel() {

    val subjects: LiveData<List<SubjectPackage>> = repository.fetchLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(subjects) { it.isEmpty() }

    fun insert(subject: Subject, scheduleList: List<Schedule>) = viewModelScope.launch {
        repository.insert(subject, scheduleList)
    }

    fun remove(subject: Subject) = viewModelScope.launch {
        repository.remove(subject)
    }

    fun update(subject: Subject, scheduleList: List<Schedule>) = viewModelScope.launch {
        repository.update(subject, scheduleList)
    }

}