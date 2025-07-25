package com.sewingfactory.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.sewingfactory.data.WorkTask
import com.sewingfactory.data.TaskStatus

@Dao
interface WorkTaskDao {
    
    @Query("SELECT * FROM work_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): LiveData<List<WorkTask>>
    
    @Query("SELECT * FROM work_tasks WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getTasksByWorker(workerId: String): LiveData<List<WorkTask>>
    
    @Query("SELECT * FROM work_tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: TaskStatus): LiveData<List<WorkTask>>
    
    @Query("SELECT * FROM work_tasks WHERE workerId = :workerId AND status = :status ORDER BY createdAt DESC")
    fun getTasksByWorkerAndStatus(workerId: String, status: TaskStatus): LiveData<List<WorkTask>>
    
    @Query("SELECT * FROM work_tasks WHERE id = :id")
    suspend fun getTaskById(id: String): WorkTask?
    
    @Query("SELECT * FROM work_tasks WHERE workerId = :workerId AND status IN ('IN_PROGRESS') LIMIT 1")
    suspend fun getActiveTaskByWorker(workerId: String): WorkTask?
    
    @Query("""
        SELECT * FROM work_tasks 
        WHERE workerId = :workerId 
        AND DATE(completedAt/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        AND status = 'COMPLETED'
        ORDER BY completedAt DESC
    """)
    suspend fun getCompletedTasksByWorkerAndDate(workerId: String, date: Long): List<WorkTask>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: WorkTask)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<WorkTask>)
    
    @Update
    suspend fun updateTask(task: WorkTask)
    
    @Delete
    suspend fun deleteTask(task: WorkTask)
    
    @Query("UPDATE work_tasks SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: TaskStatus, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE work_tasks SET workerId = :workerId, status = 'ASSIGNED', updatedAt = :updatedAt WHERE id = :id")
    suspend fun assignTaskToWorker(id: String, workerId: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE work_tasks SET completedQuantity = :quantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCompletedQuantity(id: String, quantity: Int, updatedAt: Long = System.currentTimeMillis())
}