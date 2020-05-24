package com.isaiahvonrundstedt.fokus.features.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.core.data.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel

class SearchViewModel(app: Application): BaseViewModel(app) {

    private var repository = CoreRepository.getInstance(app)
    private var initialList = ArrayList<Core>()
    private var _items: MutableLiveData<List<Core>> = MutableLiveData()
    internal var items: LiveData<List<Core>> = _items

    fun fetch(query: String) {
        repository.search(query) { items ->
            initialList.clear()
            initialList.addAll(items)
            initialList.distinctBy { it.task.taskID }.toMutableList()
            _items.postValue(initialList)
        }
    }
}