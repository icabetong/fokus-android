package com.isaiahvonrundstedt.fokus.database.repository

import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.dao.LogDAO
import com.isaiahvonrundstedt.fokus.features.log.Log
import javax.inject.Inject

class LogRepository @Inject constructor(private val logs: LogDAO) {

    fun fetch(): LiveData<List<Log>> = logs.fetch()

    suspend fun insert(log: Log) {
        logs.insert(log)
    }

    suspend fun remove(log: Log) {
        logs.remove(log)
    }

    suspend fun update(log: Log) {
        logs.update(log)
    }

    suspend fun removeLogs() {
        logs.removeLogs()
    }

}