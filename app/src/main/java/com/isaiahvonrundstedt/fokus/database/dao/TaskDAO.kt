package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskResource

@Dao
interface TaskDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun remove(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("SELECT * FROM tasks WHERE isFinished = 0")
    suspend fun fetch(): List<Task>

    @Query("SELECT COUNT(*) FROM tasks WHERE isFinished = 0")
    suspend fun fetchCount(): Int

    @Query("UPDATE tasks SET isFinished = :status WHERE taskID = :taskID")
    suspend fun setFinished(taskID: String, status: Int)

    @Transaction
    @Query("SELECT * FROM tasks LEFT JOIN subjects ON tasks.subject == subjects.subjectID ORDER BY dateAdded")
    fun fetchLiveData(): LiveData<List<TaskResource>>

}