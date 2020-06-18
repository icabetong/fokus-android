package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.history.History
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryRepository private constructor (app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var notifications = database?.notifications()

    companion object {
        private var instance: HistoryRepository? = null

        fun getInstance(app: Application): HistoryRepository {
            if (instance == null) {
                synchronized(HistoryRepository::class) {
                    instance = HistoryRepository(app)
                }
            }
            return instance!!
        }
    }

    fun fetch(): LiveData<List<History>>? = notifications?.fetch()

    fun clear() = GlobalScope.launch { notifications?.clear() }

    suspend fun insert(history: History) {
        notifications?.insert(history)
    }

    suspend fun remove(history: History) {
        notifications?.remove(history)
    }

    suspend fun update(history: History) {
        notifications?.update(history)
    }

}