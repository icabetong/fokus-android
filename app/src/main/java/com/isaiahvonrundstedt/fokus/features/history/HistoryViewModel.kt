package com.isaiahvonrundstedt.fokus.features.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.HistoryRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application): BaseViewModel(app) {

    private var repository = HistoryRepository.getInstance(app)
    private var items: LiveData<List<History>>? = repository.fetch()

    fun fetch(): LiveData<List<History>>? = items

    fun insert(history: History) = viewModelScope.launch {
        repository.insert(history)
    }

    fun remove(history: History) = viewModelScope.launch {
        repository.remove(history)
    }

    fun clear() {
        repository.clear()
    }

}