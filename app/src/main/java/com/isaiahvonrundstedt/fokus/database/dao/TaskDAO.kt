package com.isaiahvonrundstedt.fokus.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage

@Dao
interface TaskDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun remove(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("SELECT taskID FROM tasks WHERE name = :task COLLATE NOCASE")
    suspend fun checkNameUniqueness(task: String?): List<String>

    @Query("UPDATE tasks SET isFinished = :status WHERE taskID = :taskID")
    suspend fun setFinished(taskID: String, status: Int)

    @Query("SELECT * FROM tasks WHERE isFinished = 0 AND isTaskArchived = 0")
    suspend fun fetch(): List<Task>

    @Query("SELECT COUNT(*) FROM tasks WHERE isFinished = 0 AND isTaskArchived = 0")
    suspend fun fetchCount(): Int

    @Query("SELECT * FROM tasks WHERE isFinished = 0")
    suspend fun fetchAsPackage(): List<TaskPackage>

    @Transaction
    @Query("SELECT * FROM tasks LEFT JOIN subjects ON tasks.subject == subjects.subjectID WHERE isTaskArchived = 0 ORDER BY dueDate ASC")
    fun fetchLiveData(): LiveData<List<TaskPackage>>

    @Transaction
    @Query("SELECT * FROM tasks LEFT JOIN subjects ON tasks.subject == subjects.subjectID WHERE isTaskArchived = 1 ORDER BY dueDate ASC")
    fun fetchArchivedLiveData(): LiveData<List<TaskPackage>>

}