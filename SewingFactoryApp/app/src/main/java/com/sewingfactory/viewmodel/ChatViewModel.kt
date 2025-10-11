package com.sewingfactory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sewingfactory.data.ChatMessage
import com.sewingfactory.data.WorkTask
import com.sewingfactory.data.Worker
import com.sewingfactory.data.dao.ChatMessageDao
import com.sewingfactory.repository.SewingFactoryRepository
import com.sewingfactory.repository.WorkerDayStats
import com.sewingfactory.service.ChatService
import com.sewingfactory.service.ChatContext
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = com.sewingfactory.data.AppDatabase.getDatabase(application)
    private val chatMessageDao: ChatMessageDao = database.chatMessageDao()
    private val repository = SewingFactoryRepository(
        database.productDao(),
        database.workerDao(),
        database.workTaskDao(),
        database.workSessionDao()
    )
    
    private val chatService = ChatService()
    
    // LiveData для UI
    val messages: LiveData<List<ChatMessage>> = chatMessageDao.getAllMessages()
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Данные для контекста чата
    private val _currentWorker = MutableLiveData<Worker?>()
    val currentWorker: LiveData<Worker?> = _currentWorker
    
    private val _currentTask = MutableLiveData<WorkTask?>()
    val currentTask: LiveData<WorkTask?> = _currentTask
    
    private val _workerStats = MutableLiveData<WorkerDayStats?>()
    val workerStats: LiveData<WorkerDayStats?> = _workerStats
    
    init {
        // Загружаем приветственное сообщение при первом запуске
        loadWelcomeMessage()
    }
    
    fun sendMessage(content: String) {
        if (content.trim().isEmpty()) return
        
        viewModelScope.launch {
            try {
                // Сохраняем сообщение пользователя
                val userMessage = ChatMessage(
                    content = content.trim(),
                    isFromAgent = false
                )
                chatMessageDao.insertMessage(userMessage)
                
                // Получаем контекст для агента
                val context = ChatContext(
                    currentWorker = _currentWorker.value,
                    currentTask = _currentTask.value,
                    workerStats = _workerStats.value
                )
                
                // Обрабатываем сообщение агентом
                _isLoading.value = true
                val agentMessage = chatService.processUserMessage(content, context)
                chatMessageDao.insertMessage(agentMessage)
                
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при отправке сообщения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearChat() {
        viewModelScope.launch {
            try {
                chatMessageDao.deleteAllMessages()
                loadWelcomeMessage()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при очистке чата: ${e.message}"
            }
        }
    }
    
    fun setCurrentWorker(worker: Worker) {
        _currentWorker.value = worker
        loadWorkerStatsForToday(worker.id)
    }
    
    fun setCurrentTask(task: WorkTask) {
        _currentTask.value = task
    }
    
    private fun loadWelcomeMessage() {
        viewModelScope.launch {
            try {
                val lastMessage = chatMessageDao.getLastMessage()
                if (lastMessage == null) {
                    val welcomeMessage = ChatMessage(
                        content = """
                        🤖 Добро пожаловать в чат с агентом!
                        
                        Я помогу вам с вопросами по работе на швейном производстве.
                        
                        Вы можете спросить:
                        • О текущем задании
                        • О статистике работы
                        • О выбранном работнике
                        • О времени
                        
                        Просто напишите "помощь" для получения списка команд.
                        """.trimIndent(),
                        isFromAgent = true
                    )
                    chatMessageDao.insertMessage(welcomeMessage)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке приветственного сообщения: ${e.message}"
            }
        }
    }
    
    private fun loadWorkerStatsForToday(workerId: String) {
        viewModelScope.launch {
            try {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val stats = repository.getWorkerStatsByDate(workerId, today)
                _workerStats.value = stats
            } catch (e: Exception) {
                // Игнорируем ошибки загрузки статистики
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}