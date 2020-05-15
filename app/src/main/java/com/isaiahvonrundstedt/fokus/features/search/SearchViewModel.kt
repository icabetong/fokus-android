package com.isaiahvonrundstedt.fokus.features.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel

class SearchViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = CoreRepository(app)
    private var initialList = ArrayList<Core>()
    private var _items: MutableLiveData<List<Core>> = MutableLiveData()
    internal var items: LiveData<List<Core>> = _items

    fun fetch(query: String) {
        dataStore.search(query) { items ->
            initialList.clear()
            initialList.addAll(items)
            initialList.distinctBy { it.task.taskID }.toMutableList()
            _items.postValue(initialList)
        }
    }
}