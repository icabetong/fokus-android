package com.isaiahvonrundstedt.fokus.database.dao

import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment

@Dao
interface AttachmentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(attachment: Attachment)

    @Delete
    fun remove(attachment: Attachment)

    @Update
    fun update(attachment: Attachment)

    @Query("SELECT * FROM attachments")
    suspend fun fetch(): List<Attachment>

    @Query("DELETE FROM attachments WHERE task = :id")
    fun removeUsingTaskID(id: String)
}