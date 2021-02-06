package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage

@Dao
interface EventDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Delete
    suspend fun remove(event: Event)

    @Update
    suspend fun update(event: Event)

    @Query("SELECT * FROM events")
    suspend fun fetch(): List<Event>

    @Query("SELECT * FROM events")
    suspend fun fetchPackage(): List<EventPackage>

    @Transaction
    @Query("SELECT * FROM events LEFT JOIN subjects ON events.subject == subjects.subjectID WHERE isEventArchived = 1 ORDER BY schedule ASC")
    fun fetchLiveData(): LiveData<List<EventPackage>>

    @Transaction
    @Query("SELECT * FROM events LEFT JOIN subjects ON events.subject == subjects.subjectID WHERE isEventArchived = 1 ORDER BY schedule ASC")
    fun fetchArchivedLiveData(): LiveData<List<EventPackage>>

}