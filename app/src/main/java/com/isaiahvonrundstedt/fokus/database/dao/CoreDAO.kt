package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.isaiahvonrundstedt.fokus.features.core.data.Core

@Dao
interface CoreDAO {

    @Transaction
    @Query("SELECT * FROM tasks INNER JOIN subjects ON tasks.subjectID == subjects.id ORDER BY dateAdded")
    fun fetch(): LiveData<List<Core>>

    @Transaction
    @Query("SELECT * FROM tasks INNER JOIN subjects ON tasks.subjectID == subjects.id WHERE tasks.name LIKE :query OR tasks.notes LIKE :query OR subjects.code LIKE :query OR subjects.description LIKE :query ORDER BY dateAdded")
    fun search(query: String): List<Core>
}