package com.isaiahvonrundstedt.fokus.features.archived

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel

class ArchivedViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = CoreRepository(app)
    private var items: LiveData<List<Core>>? = dataStore.fetchArchived()

    fun fetch(): LiveData<List<Core>>? = items

    fun insert(core: Core) {
        dataStore.insert(core)
    }

    fun update(core: Core) {
        dataStore.insert(core)
    }

    fun remove(core: Core) {
        dataStore.insert(core)
    }

    fun clear() {
        dataStore.clearArchived()
    }

}