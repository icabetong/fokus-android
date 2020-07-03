package com.isaiahvonrundstedt.fokus.database.dao

import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment

@Dao
interface AttachmentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: Attachment)

    @Delete
    suspend fun remove(attachment: Attachment)

    @Update
    suspend fun update(attachment: Attachment)

    @Query("DELETE FROM attachments WHERE task = :id")
    suspend fun removeUsingTaskID(id: String)
}