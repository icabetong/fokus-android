package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.log.Log

@Dao
interface LogDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: Log)

    @Delete
    suspend fun remove(log: Log)

    @Update
    suspend fun update(log: Log)

    @Query("SELECT * FROM logs")
    fun fetch(): LiveData<List<Log>>

    @Query("DELETE FROM logs")
    suspend fun clear()

}