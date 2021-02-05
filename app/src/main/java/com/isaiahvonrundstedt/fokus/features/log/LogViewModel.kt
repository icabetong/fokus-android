package com.isaiahvonrundstedt.fokus.features.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository
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