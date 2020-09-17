package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.log.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LogRepository private constructor(app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var logs = database.logs()

    companion object {
        private var instance: LogRepository? = null

        fun getInstance(app: Application): LogRepository {
            if (instance == null) {
                synchronized(LogRepository::class) {
                    instance = LogRepository(app)
                }
            }
            return instance!!
        }
    }

    fun fetch(): LiveData<List<Log>>? = logs.fetch()

    fun clear() = GlobalScope.launch { logs.clear() }

    suspend fun insert(log: Log) {
        logs.insert(log)
    }

    suspend fun remove(log: Log) {
        logs.remove(log)
    }

    suspend fun update(log: Log) {
        logs.update(log)
    }

}