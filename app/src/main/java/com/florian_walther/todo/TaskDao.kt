package com.florian_walther.todo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE name LIKE '%' || :query || '%' ORDER BY isImportant DESC")
    fun getTasks(query: String): Flow<List<Task>>

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}