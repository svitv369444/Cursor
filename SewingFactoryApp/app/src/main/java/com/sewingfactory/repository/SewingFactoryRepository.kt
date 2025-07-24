package com.sewingfactory.repository

import androidx.lifecycle.LiveData
import com.sewingfactory.data.*
import com.sewingfactory.data.dao.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class SewingFactoryRepository(
    private val productDao: ProductDao,
    private val workerDao: WorkerDao,
    private val workTaskDao: WorkTaskDao,
    private val workSessionDao: WorkSessionDao
) {
    
    // Products
    fun getAllActiveProducts(): LiveData<List<Product>> = productDao.getAllActiveProducts()
    
    suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }
    
    suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }
    
    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        productDao.searchProducts("%$query%")
    }
    
    // Workers
    fun getAllActiveWorkers(): LiveData<List<Worker>> = workerDao.getAllActiveWorkers()
    
    suspend fun getWorkerById(id: String): Worker? = withContext(Dispatchers.IO) {
        workerDao.getWorkerById(id)
    }
    
    suspend fun insertWorker(worker: Worker) = withContext(Dispatchers.IO) {
        workerDao.insertWorker(worker)
    }
    
    // Work Tasks
    fun getAllTasks(): LiveData<List<WorkTask>> = workTaskDao.getAllTasks()
    
    fun getTasksByWorker(workerId: String): LiveData<List<WorkTask>> = 
        workTaskDao.getTasksByWorker(workerId)
    
    suspend fun getTaskById(id: String): WorkTask? = withContext(Dispatchers.IO) {
        workTaskDao.getTaskById(id)
    }
    
    suspend fun insertTask(task: WorkTask) = withContext(Dispatchers.IO) {
        workTaskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: WorkTask) = withContext(Dispatchers.IO) {
        workTaskDao.updateTask(task)
    }
    
    suspend fun assignTaskToWorker(taskId: String, workerId: String) = withContext(Dispatchers.IO) {
        workTaskDao.assignTaskToWorker(taskId, workerId)
    }
    
    suspend fun startTask(taskId: String, workerId: String): Long = withContext(Dispatchers.IO) {
        // Обновляем статус задания
        workTaskDao.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS)
        workTaskDao.updateTask(
            workTaskDao.getTaskById(taskId)!!.copy(
                startedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        // Создаем новую рабочую сессию
        val session = WorkSession(
            taskId = taskId,
            workerId = workerId,
            startTime = System.currentTimeMillis()
        )
        workSessionDao.insertSession(session)
    }
    
    suspend fun completeTask(taskId: String, completedQuantity: Int) = withContext(Dispatchers.IO) {
        val task = workTaskDao.getTaskById(taskId)
        if (task != null) {
            val updatedTask = task.copy(
                completedQuantity = completedQuantity,
                status = if (completedQuantity >= task.quantity) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS,
                completedAt = if (completedQuantity >= task.quantity) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis()
            )
            workTaskDao.updateTask(updatedTask)
            
            // Завершаем активную сессию
            val activeSession = workSessionDao.getActiveSessionByWorker(task.workerId!!)
            if (activeSession != null) {
                workSessionDao.updateSession(
                    activeSession.copy(
                        endTime = System.currentTimeMillis(),
                        completedQuantity = completedQuantity,
                        isActive = false
                    )
                )
            }
        }
    }
    
    // Work Sessions
    fun getSessionsByWorker(workerId: String): LiveData<List<WorkSession>> = 
        workSessionDao.getSessionsByWorker(workerId)
    
    suspend fun getActiveSessionByWorker(workerId: String): WorkSession? = withContext(Dispatchers.IO) {
        workSessionDao.getActiveSessionByWorker(workerId)
    }
    
    suspend fun getSessionsByWorkerAndDate(workerId: String, date: Long): List<WorkSession> = 
        withContext(Dispatchers.IO) {
            workSessionDao.getSessionsByWorkerAndDate(workerId, date)
        }
    
    suspend fun getTotalEarningsByWorkerAndDate(workerId: String, date: Long): Double = 
        withContext(Dispatchers.IO) {
            val sessions = workSessionDao.getSessionsByWorkerAndDate(workerId, date)
            var totalEarnings = 0.0
            
            for (session in sessions) {
                val task = workTaskDao.getTaskById(session.taskId)
                val product = task?.let { productDao.getProductById(it.productId) }
                if (product != null) {
                    totalEarnings += product.price * session.completedQuantity
                }
            }
            
            totalEarnings
        }
    
    suspend fun getWorkerStatsByDate(workerId: String, date: Long): WorkerDayStats = 
        withContext(Dispatchers.IO) {
            val sessions = workSessionDao.getSessionsByWorkerAndDate(workerId, date)
            val completedTasks = workTaskDao.getCompletedTasksByWorkerAndDate(workerId, date)
            val totalQuantity = workSessionDao.getTotalCompletedQuantityByWorkerAndDate(workerId, date) ?: 0
            val totalWorkTime = workSessionDao.getTotalWorkTimeByWorkerAndDate(workerId, date) ?: 0L
            val totalEarnings = getTotalEarningsByWorkerAndDate(workerId, date)
            
            WorkerDayStats(
                date = date,
                totalQuantity = totalQuantity,
                totalWorkTimeMinutes = totalWorkTime,
                totalEarnings = totalEarnings,
                completedTasksCount = completedTasks.size,
                sessionsCount = sessions.size
            )
        }
}

data class WorkerDayStats(
    val date: Long,
    val totalQuantity: Int,
    val totalWorkTimeMinutes: Long,
    val totalEarnings: Double,
    val completedTasksCount: Int,
    val sessionsCount: Int
)