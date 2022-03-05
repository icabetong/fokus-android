package com.isaiahvonrundstedt.fokus.features.event.archived

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedEventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    val items: LiveData<List<EventPackage>> = eventRepository.fetchArchivedLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(items) { it.isEmpty() }

    fun removeFromArchive(eventPackage: EventPackage) = viewModelScope.launch {
        eventPackage.event.isEventArchived = false
        eventRepository.update(eventPackage.event)
    }

}