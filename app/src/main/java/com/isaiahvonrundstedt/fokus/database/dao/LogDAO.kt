package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.log.Log

@Dao
interface LogDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(log: Log)

    @Delete
    fun remove(log: Log)

    @Update
    fun update(log: Log)

    @Query("SELECT * FROM logs")
    fun fetchCore(): List<Log>

    @Query("DELETE FROM logs")
    fun removeLogs()

    @Query("SELECT * FROM logs ORDER BY dateTimeTriggered ASC")
    fun fetch(): LiveData<List<Log>>


}