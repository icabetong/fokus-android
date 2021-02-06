package com.isaiahvonrundstedt.fokus.features.archived.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchivedEventViewModel @Inject constructor(
    private val eventRepository: EventRepository
): ViewModel() {

    val items: LiveData<List<EventPackage>> = eventRepository.fetchArchivedLiveData()
}