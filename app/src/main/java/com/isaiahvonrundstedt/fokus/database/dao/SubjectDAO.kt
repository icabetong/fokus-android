package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

@Dao
interface SubjectDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subject: Subject)

    @Delete
    fun remove(subject: Subject)

    @Update
    fun update(subject: Subject)

    @Query("SELECT subjectID FROM subjects WHERE code = :code COLLATE NOCASE")
    fun checkCodeUniqueness(code: String?): List<String>

    @Query("SELECT * FROM subjects")
    fun fetch(): List<Subject>

    @Query("SELECT * FROM subjects")
    fun fetchAsPackage(): List<SubjectPackage>

    @Transaction
    @Query("SELECT * FROM subjects WHERE isSubjectArchived = 0 ORDER BY code ASC")
    fun fetchLiveData(): LiveData<List<SubjectPackage>>

    @Transaction
    @Query("SELECT * FROM subjects WHERE isSubjectArchived = 1 ORDER BY code ASC")
    fun fetchArchivedLiveData(): LiveData<List<SubjectPackage>>


}