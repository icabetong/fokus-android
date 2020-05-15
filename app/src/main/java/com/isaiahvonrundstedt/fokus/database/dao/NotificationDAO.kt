package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.notifications.Notification

@Dao
interface NotificationDAO {

    @Insert
    suspend fun insert(notification: Notification)

    @Delete
    suspend fun remove(notification: Notification)

    @Update
    suspend fun update(notification: Notification)

    @Query("SELECT * FROM notifications")
    fun fetch(): LiveData<List<Notification>>

    @Query("DELETE FROM notifications")
    suspend fun clear()

}