package com.isaiahvonrundstedt.fokus.features.subject.picker

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class SubjectPickerViewModel @ViewModelInject constructor(
    repository: SubjectRepository,
    @Assisted
    savedStateHandle: SavedStateHandle
): ViewModel() {

    val subjects: LiveData<List<SubjectPackage>> = repository.fetchLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(subjects) { it.isEmpty() }

}