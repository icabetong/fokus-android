package com.isaiahvonrundstedt.fokus.features.task

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.repository.CoreRepository
import com.isaiahvonrundstedt.fokus.features.core.Core
import com.isaiahvonrundstedt.fokus.features.core.work.DeadlineScheduler
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager

class TaskViewModel(private var app: Application): BaseViewModel(app) {

    private var dataStore = CoreRepository(app)
    private var workManager = WorkManager.getInstance(app)
    private var items: LiveData<List<Core>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Core>>? = items

    fun insert(core: Core) {
        dataStore.insert(core)

        if (PreferenceManager(app).remindWhenDue) {
            val data = BaseWorker.convertTaskToData(core.task)
            val request = OneTimeWorkRequestBuilder<DeadlineScheduler>()
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    fun remove(core: Core) {
        dataStore.remove(core)

        workManager.cancelUniqueWork(core.task.taskID)
    }

    fun update(core: Core) {
        dataStore.update(core)

        if (PreferenceManager(app).remindWhenDue && !core.task.isArchived && !core.task.isFinished) {
            workManager.cancelUniqueWork(core.task.taskID)
            val data = BaseWorker.convertTaskToData(core.task)
            val request = OneTimeWorkRequestBuilder<DeadlineScheduler>()
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

}