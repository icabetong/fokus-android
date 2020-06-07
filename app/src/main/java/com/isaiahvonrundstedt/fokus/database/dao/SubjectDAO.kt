package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.subject.Subject

@Dao
interface SubjectDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject)

    @Delete
    suspend fun remove(subject: Subject)

    @Update
    suspend fun update(subject: Subject)

    @Query("SELECT * FROM subjects")
    fun fetch(): LiveData<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun fetchItem(id: String): Subject

}