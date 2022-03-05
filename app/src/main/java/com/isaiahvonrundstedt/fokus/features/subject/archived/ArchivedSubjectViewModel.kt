package com.isaiahvonrundstedt.fokus.features.subject.archived

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedSubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val items: LiveData<List<SubjectPackage>> = subjectRepository.fetchArchivedLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(items) { it.isEmpty() }

    fun removeFromArchive(subjectPackage: SubjectPackage) = viewModelScope.launch {
        subjectPackage.subject.isSubjectArchived = false
        subjectRepository.update(subjectPackage.subject)
    }

}