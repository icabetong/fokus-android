package com.isaiahvonrundstedt.fokus.features.subject.picker

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class SubjectPickerViewModel(app: Application): BaseViewModel(app) {

    private val repository = SubjectRepository.getInstance(app)

    val subjects: LiveData<List<SubjectPackage>> = repository.fetchLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(subjects) { it.isEmpty() }

}