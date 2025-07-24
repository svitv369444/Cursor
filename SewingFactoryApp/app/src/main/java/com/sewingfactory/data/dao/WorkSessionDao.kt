package com.sewingfactory.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.sewingfactory.data.WorkSession

@Dao
interface WorkSessionDao {
    
    @Query("SELECT * FROM work_sessions ORDER BY startTime DESC")
    fun getAllSessions(): LiveData<List<WorkSession>>
    
    @Query("SELECT * FROM work_sessions WHERE workerId = :workerId ORDER BY startTime DESC")
    fun getSessionsByWorker(workerId: String): LiveData<List<WorkSession>>
    
    @Query("SELECT * FROM work_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getSessionsByTask(taskId: String): LiveData<List<WorkSession>>
    
    @Query("SELECT * FROM work_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkSession?
    
    @Query("SELECT * FROM work_sessions WHERE workerId = :workerId AND isActive = 1 LIMIT 1")
    suspend fun getActiveSessionByWorker(workerId: String): WorkSession?
    
    @Query("""
        SELECT * FROM work_sessions 
        WHERE workerId = :workerId 
        AND DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        ORDER BY startTime DESC
    """)
    suspend fun getSessionsByWorkerAndDate(workerId: String, date: Long): List<WorkSession>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkSession): Long
    
    @Update
    suspend fun updateSession(session: WorkSession)
    
    @Delete
    suspend fun deleteSession(session: WorkSession)
    
    @Query("UPDATE work_sessions SET endTime = :endTime, isActive = 0 WHERE id = :id")
    suspend fun endSession(id: Long, endTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE work_sessions SET completedQuantity = :quantity WHERE id = :id")
    suspend fun updateCompletedQuantity(id: Long, quantity: Int)
    
    @Query("""
        SELECT SUM(completedQuantity) FROM work_sessions 
        WHERE workerId = :workerId 
        AND DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """)
    suspend fun getTotalCompletedQuantityByWorkerAndDate(workerId: String, date: Long): Int?
    
    @Query("""
        SELECT SUM(CASE WHEN endTime IS NOT NULL THEN (endTime - startTime) ELSE 0 END) / (1000 * 60) as totalMinutes
        FROM work_sessions 
        WHERE workerId = :workerId 
        AND DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """)
    suspend fun getTotalWorkTimeByWorkerAndDate(workerId: String, date: Long): Long?
}