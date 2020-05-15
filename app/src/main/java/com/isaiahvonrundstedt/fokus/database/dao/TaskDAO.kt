package com.isaiahvonrundstedt.fokus.database.dao

import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.task.Task

@Dao
interface TaskDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun remove(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("SELECT * FROM tasks WHERE isFinished = 0 AND isArchived = 0")
    suspend fun fetch(): List<Task>

    @Query("SELECT COUNT(*) FROM tasks WHERE isFinished = 0 AND isArchived = 0")
    suspend fun fetchCount(): Int

    @Query("DELETE FROM tasks WHERE isArchived = 1")
    suspend fun clearArchived()

}