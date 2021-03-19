package com.florian_walther.todo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE name LIKE '%' || :query || '%' ORDER BY is_important DESC")
    fun getTasks(query: String): Flow<List<Task>>

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getTasksSortByDate(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortByName(query, hideCompleted)
        }

    @Query("SELECT * FROM tasks WHERE (is_completed != :hideCompleted OR is_completed = 0) AND name LIKE '%' || :query || '%' ORDER BY is_important DESC, name")
    fun getTasksSortByName(query: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE (is_completed != :hideCompleted OR is_completed = 0) AND name LIKE '%' || :query || '%' ORDER BY is_important DESC, date")
    fun getTasksSortByDate(query: String, hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE is_completed=1")
    suspend fun deleteCompleted()
}