package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.core.Core
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CoreRepository (app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var dataBundles = database?.bundle()
    private var tasks = database?.tasks()
    private var attachments = database?.attachment()

    fun fetch(): LiveData<List<Core>>? = dataBundles?.fetch()

    fun fetchArchived(): LiveData<List<Core>>? = dataBundles?.fetchArchived()

    fun search(query: String, onSearch:(List<Core>) -> Unit) = GlobalScope.launch {
        onSearch(dataBundles?.search("%$query%") ?: emptyList())
    }

    fun clearArchived() = GlobalScope.launch { tasks?.clearArchived() }

    fun insert(core: Core) = GlobalScope.launch {
        tasks?.insert(core.task)
        core.attachmentList.forEach { attachments?.insert(it) }
    }

    fun remove(core: Core) = GlobalScope.launch {
        tasks?.remove(core.task)
    }

    fun update(core: Core) = GlobalScope.launch {
        tasks?.update(core.task)
        attachments?.removeUsingTaskID(core.task.taskID)
        core.attachmentList.forEach { attachments?.insert(it) }
    }
}