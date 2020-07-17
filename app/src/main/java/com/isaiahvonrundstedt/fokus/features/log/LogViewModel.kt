package com.isaiahvonrundstedt.fokus.features.log

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.LogRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class LogViewModel(app: Application) : BaseViewModel(app) {

    private var repository = LogRepository.getInstance(app)
    private var items: LiveData<List<Log>>? = repository.fetch()

    fun fetch(): LiveData<List<Log>>? = items

    fun insert(log: Log) = viewModelScope.launch {
        repository.insert(log)
    }

    fun remove(log: Log) = viewModelScope.launch {
        repository.remove(log)
    }

    fun clear() {
        repository.clear()
    }

}