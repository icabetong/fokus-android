package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.log.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LogRepository private constructor(app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var histories = database?.logs()

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

    fun fetch(): LiveData<List<Log>>? = histories?.fetch()

    fun clear() = GlobalScope.launch { histories?.clear() }

    suspend fun insert(log: Log) {
        histories?.insert(log)
    }

    suspend fun remove(log: Log) {
        histories?.remove(log)
    }

    suspend fun update(log: Log) {
        histories?.update(log)
    }

}