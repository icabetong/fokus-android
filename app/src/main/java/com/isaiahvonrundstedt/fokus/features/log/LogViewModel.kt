package com.isaiahvonrundstedt.fokus.features.log

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class LogViewModel(app: Application) : BaseViewModel(app) {

    private var repository = LogRepository.getInstance(app)
    private var _logs: LiveData<List<Log>> = repository.fetch()

    val logs: MediatorLiveData<List<Log>> = MediatorLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(logs) { it.isNullOrEmpty() }

    init {
        logs.addSource(_logs) {
            logs.value = it
        }
    }

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