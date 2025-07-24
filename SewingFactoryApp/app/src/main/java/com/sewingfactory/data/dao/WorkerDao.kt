package com.sewingfactory.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.sewingfactory.data.Worker

@Dao
interface WorkerDao {
    
    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY lastName, firstName")
    fun getAllActiveWorkers(): LiveData<List<Worker>>
    
    @Query("SELECT * FROM workers ORDER BY lastName, firstName")
    fun getAllWorkers(): LiveData<List<Worker>>
    
    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: String): Worker?
    
    @Query("SELECT * FROM workers WHERE lastName LIKE :searchQuery OR firstName LIKE :searchQuery AND isActive = 1")
    suspend fun searchWorkers(searchQuery: String): List<Worker>
    
    @Query("SELECT * FROM workers WHERE department = :department AND isActive = 1")
    suspend fun getWorkersByDepartment(department: String): List<Worker>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<Worker>)
    
    @Update
    suspend fun updateWorker(worker: Worker)
    
    @Delete
    suspend fun deleteWorker(worker: Worker)
    
    @Query("UPDATE workers SET isActive = 0 WHERE id = :id")
    suspend fun deactivateWorker(id: String)
    
    @Query("SELECT DISTINCT department FROM workers WHERE isActive = 1 ORDER BY department")
    suspend fun getAllDepartments(): List<String>
}