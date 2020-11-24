package com.isaiahvonrundstedt.fokus.features.log

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import kotlinx.coroutines.launch

class LogViewModel @ViewModelInject constructor(
    private val repository: LogRepository,
    @Assisted
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val logs: LiveData<List<Log>> = repository.fetch()
    val isEmpty: LiveData<Boolean> = Transformations.map(logs) { it.isNullOrEmpty() }

    fun insert(log: Log) = viewModelScope.launch {
        repository.insert(log)
    }

    fun remove(log: Log) = viewModelScope.launch {
        repository.remove(log)
    }

    fun removeLogs() = viewModelScope.launch {
        repository.removeLogs()
    }

}