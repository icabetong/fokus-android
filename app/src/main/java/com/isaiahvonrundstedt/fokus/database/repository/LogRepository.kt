package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.log.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LogRepository private constructor(context: Context) {

    private var database = AppDatabase.getInstance(context)
    private var logs = database.logs()

    companion object {
        private var instance: LogRepository? = null

        fun getInstance(context: Context): LogRepository {
            if (instance == null) {
                synchronized(LogRepository::class) {
                    instance = LogRepository(context)
                }
            }
            return instance!!
        }
    }

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