package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventResource

@Dao
interface EventDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Delete
    suspend fun remove(event: Event)

    @Update
    suspend fun update(event: Event)

    @Query("SELECT * FROM events ORDER BY schedule ASC")
    suspend fun fetch(): List<Event>

    @Transaction
    @Query(
        "SELECT * FROM events LEFT JOIN subjects ON events.subject == subjects.subjectID ORDER BY schedule"
    )
    fun fetchLiveData(): LiveData<List<EventResource>>

}