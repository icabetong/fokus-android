package com.isaiahvonrundstedt.fokus.features.notifications

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.NotificationRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class NotificationViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = NotificationRepository(app)
    private var items: LiveData<List<Notification>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Notification>>? = items

    fun insert(notification: Notification) = viewModelScope.launch {
        dataStore.insert(notification)
    }

    fun remove(notification: Notification) = viewModelScope.launch {
        dataStore.remove(notification)
    }

    fun clear() {
        dataStore.clear()
    }

}