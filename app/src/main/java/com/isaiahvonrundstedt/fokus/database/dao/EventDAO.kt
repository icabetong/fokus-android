package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage

@Dao
interface EventDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: Event)

    @Delete
    fun remove(event: Event)

    @Update
    fun update(event: Event)

    @Query("SELECT eventID FROM events WHERE name = :event AND DATE(schedule) = DATE(:schedule) COLLATE NOCASE AND eventId != :eventId")
    suspend fun checkNameUniqueness(event: String?, schedule: String?, eventId: String?): List<String>

    @Query("SELECT * FROM events")
    suspend fun fetch(): List<Event>

    @Query("SELECT * FROM events")
    suspend fun fetchPackage(): List<EventPackage>

    @Transaction
    @Query("SELECT * FROM events LEFT JOIN subjects ON events.subject == subjects.subjectID WHERE isEventArchived = 0 ORDER BY schedule ASC")
    fun fetchLiveData(): LiveData<List<EventPackage>>

    @Transaction
    @Query("SELECT * FROM events LEFT JOIN subjects ON events.subject == subjects.subjectID WHERE isEventArchived = 1 ORDER BY schedule ASC")
    fun fetchArchivedLiveData(): LiveData<List<EventPackage>>

}