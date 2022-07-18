package com.isaiahvonrundstedt.fokus.database.dao

import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule

@Dao
interface ScheduleDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg schedule: Schedule)

    @Delete
    fun remove(schedule: Schedule)

    @Update
    fun update(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE subject = :id")
    fun removeUsingSubjectID(id: String)

    @Query("SELECT * FROM schedules")
    fun fetch(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE subject = :id")
    fun fetchUsingID(id: String?): List<Schedule>

}