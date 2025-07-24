package com.sewingfactory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sewingfactory.data.*
import com.sewingfactory.repository.SewingFactoryRepository
import com.sewingfactory.repository.WorkerDayStats
import com.sewingfactory.api.ApiClient
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = SewingFactoryRepository(
        database.productDao(),
        database.workerDao(),
        database.workTaskDao(),
        database.workSessionDao()
    )
    
    private val apiService = ApiClient.oneCApiService
    
    // LiveData для UI
    val allProducts: LiveData<List<Product>> = repository.getAllActiveProducts()
    val allWorkers: LiveData<List<Worker>> = repository.getAllActiveWorkers()
    val allTasks: LiveData<List<WorkTask>> = repository.getAllTasks()
    
    private val _currentWorker = MutableLiveData<Worker?>()
    val currentWorker: LiveData<Worker?> = _currentWorker
    
    private val _currentTask = MutableLiveData<WorkTask?>()
    val currentTask: LiveData<WorkTask?> = _currentTask
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _workerStats = MutableLiveData<WorkerDayStats?>()
    val workerStats: LiveData<WorkerDayStats?> = _workerStats
    
    fun setCurrentWorker(worker: Worker) {
        _currentWorker.value = worker
        loadWorkerTasks(worker.id)
        loadWorkerStatsForToday(worker.id)
    }
    
    fun scanQRCode(qrContent: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val task = repository.getTaskById(qrContent)
                if (task != null) {
                    _currentTask.value = task
                    val worker = _currentWorker.value
                    if (worker != null && task.workerId == null) {
                        // Назначаем задание работнику
                        repository.assignTaskToWorker(task.id, worker.id)
                        _currentTask.value = task.copy(workerId = worker.id)
                    }
                } else {
                    // Попробуем получить задание из 1С
                    syncTaskFromOneC(qrContent)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при сканировании QR кода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startTask() {
        viewModelScope.launch {
            try {
                val task = _currentTask.value
                val worker = _currentWorker.value
                if (task != null && worker != null) {
                    _isLoading.value = true
                    repository.startTask(task.id, worker.id)
                    _currentTask.value = task.copy(
                        status = TaskStatus.IN_PROGRESS,
                        startedAt = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при начале работы: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun completeTask(completedQuantity: Int) {
        viewModelScope.launch {
            try {
                val task = _currentTask.value
                if (task != null) {
                    _isLoading.value = true
                    repository.completeTask(task.id, completedQuantity)
                    
                    // Синхронизируем с 1С
                    syncTaskCompletionToOneC(task.id, completedQuantity)
                    
                    _currentTask.value = null
                    
                    // Обновляем статистику работника
                    val worker = _currentWorker.value
                    if (worker != null) {
                        loadWorkerStatsForToday(worker.id)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при завершении задания: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadWorkerStatsForDate(workerId: String, date: Long) {
        viewModelScope.launch {
            try {
                val stats = repository.getWorkerStatsByDate(workerId, date)
                _workerStats.value = stats
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке статистики: ${e.message}"
            }
        }
    }
    
    private fun loadWorkerStatsForToday(workerId: String) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        loadWorkerStatsForDate(workerId, today)
    }
    
    private fun loadWorkerTasks(workerId: String) {
        // Задания работника уже загружаются через LiveData в UI
    }
    
    fun syncWithOneC() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Синхронизируем продукты
                val productsResponse = apiService.getProducts()
                if (productsResponse.isSuccessful) {
                    productsResponse.body()?.let { products ->
                        products.forEach { repository.insertProduct(it) }
                    }
                }
                
                // Синхронизируем работников
                val workersResponse = apiService.getWorkers()
                if (workersResponse.isSuccessful) {
                    workersResponse.body()?.let { workers ->
                        workers.forEach { repository.insertWorker(it) }
                    }
                }
                
                // Синхронизируем задания
                val tasksResponse = apiService.getTasks()
                if (tasksResponse.isSuccessful) {
                    tasksResponse.body()?.let { tasks ->
                        tasks.forEach { repository.insertTask(it) }
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка синхронизации с 1С: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun syncTaskFromOneC(taskId: String) {
        try {
            val response = apiService.getTask(taskId)
            if (response.isSuccessful) {
                response.body()?.let { task ->
                    repository.insertTask(task)
                    _currentTask.value = task
                }
            } else {
                _errorMessage.value = "Задание с QR кодом $taskId не найдено"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка при получении задания из 1С: ${e.message}"
        }
    }
    
    private suspend fun syncTaskCompletionToOneC(taskId: String, completedQuantity: Int) {
        try {
            val worker = _currentWorker.value
            if (worker != null) {
                val completion = com.sewingfactory.api.TaskCompletion(
                    completedQuantity = completedQuantity,
                    completedAt = System.currentTimeMillis(),
                    workerId = worker.id
                )
                apiService.completeTask(taskId, completion)
            }
        } catch (e: Exception) {
            // Логируем ошибку, но не показываем пользователю
            // так как основная операция (локальное сохранение) уже выполнена
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}