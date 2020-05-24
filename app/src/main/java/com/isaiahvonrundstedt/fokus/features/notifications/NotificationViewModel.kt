package com.isaiahvonrundstedt.fokus.features.notifications

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.NotificationRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class NotificationViewModel(app: Application): BaseViewModel(app) {

    private var repository = NotificationRepository(app)
    private var items: LiveData<List<Notification>>? = repository.fetch()

    fun fetch(): LiveData<List<Notification>>? = items

    fun insert(notification: Notification) = viewModelScope.launch {
        repository.insert(notification)
    }

    fun remove(notification: Notification) = viewModelScope.launch {
        repository.remove(notification)
    }

    fun clear() {
        repository.clear()
    }

}