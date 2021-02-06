package com.isaiahvonrundstedt.fokus.features.archived.subject

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchivedSubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository
): ViewModel() {

    val items: LiveData<List<SubjectPackage>> = subjectRepository.fetchArchivedLiveData()
}