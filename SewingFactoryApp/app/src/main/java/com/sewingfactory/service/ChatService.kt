package com.sewingfactory.service

import com.sewingfactory.data.ChatMessage
import com.sewingfactory.data.MessageType
import com.sewingfactory.data.WorkTask
import com.sewingfactory.data.Worker
import com.sewingfactory.repository.WorkerDayStats
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class ChatService {
    
    suspend fun processUserMessage(userMessage: String, context: ChatContext): ChatMessage {
        // Имитируем задержку обработки
        delay(1000)
        
        val response = generateAgentResponse(userMessage, context)
        return ChatMessage(
            content = response,
            isFromAgent = true,
            messageType = determineMessageType(userMessage)
        )
    }
    
    private fun generateAgentResponse(userMessage: String, context: ChatContext): String {
        val message = userMessage.lowercase()
        
        return when {
            message.contains("привет") || message.contains("здравствуй") -> {
                "Привет! Я ваш помощник по швейному производству. Чем могу помочь?"
            }
            message.contains("задание") || message.contains("задача") -> {
                generateTaskInfoResponse(context.currentTask)
            }
            message.contains("статистика") || message.contains("статистик") -> {
                generateStatsResponse(context.workerStats, context.currentWorker)
            }
            message.contains("помощь") || message.contains("help") -> {
                generateHelpResponse()
            }
            message.contains("время") || message.contains("сколько") -> {
                "Текущее время: ${getCurrentTime()}"
            }
            message.contains("работник") || message.contains("сотрудник") -> {
                generateWorkerInfoResponse(context.currentWorker)
            }
            else -> {
                generateDefaultResponse()
            }
        }
    }
    
    private fun generateTaskInfoResponse(task: WorkTask?): String {
        return if (task != null) {
            """
            📋 Информация о текущем задании:
            
            ID задания: ${task.id}
            Количество: ${task.quantity}
            Выполнено: ${task.completedQuantity}
            Статус: ${getStatusText(task.status)}
            ${if (task.startedAt != null) "Начато: ${formatDateTime(task.startedAt!!)}" else ""}
            """.trimIndent()
        } else {
            "У вас нет активного задания. Отсканируйте QR код для получения нового задания."
        }
    }
    
    private fun generateStatsResponse(stats: WorkerDayStats?, worker: Worker?): String {
        return if (stats != null && worker != null) {
            """
            📊 Статистика за сегодня:
            
            Работник: ${worker.fullName}
            Выполнено изделий: ${stats.totalQuantity}
            Завершено заданий: ${stats.completedTasksCount}
            Время работы: ${stats.totalWorkTimeMinutes} мин.
            Заработано: ${String.format("%.2f", stats.totalEarnings)} руб.
            """.trimIndent()
        } else {
            "Статистика недоступна. Выберите работника для просмотра статистики."
        }
    }
    
    private fun generateWorkerInfoResponse(worker: Worker?): String {
        return if (worker != null) {
            """
            👤 Информация о работнике:
            
            Имя: ${worker.fullName}
            ID: ${worker.id}
            Должность: ${worker.position}
            """.trimIndent()
        } else {
            "Работник не выбран. Нажмите кнопку 'Выбрать' для выбора работника."
        }
    }
    
    private fun generateHelpResponse(): String {
        return """
        🤖 Доступные команды:
        
        • "задание" - информация о текущем задании
        • "статистика" - статистика за сегодня
        • "работник" - информация о текущем работнике
        • "время" - текущее время
        • "помощь" - показать это сообщение
        
        Также можете задать любой вопрос о работе!
        """.trimIndent()
    }
    
    private fun generateDefaultResponse(): String {
        val responses = listOf(
            "Понял! Чем еще могу помочь?",
            "Интересно! Расскажите подробнее.",
            "Хорошо, что еще вас интересует?",
            "Понял ваше сообщение. Есть еще вопросы?",
            "Спасибо за сообщение! Чем могу помочь?"
        )
        return responses.random()
    }
    
    private fun determineMessageType(userMessage: String): MessageType {
        val message = userMessage.lowercase()
        return when {
            message.contains("задание") || message.contains("задача") -> MessageType.TASK_INFO
            message.contains("статистика") || message.contains("статистик") -> MessageType.STATISTICS
            message.contains("помощь") || message.contains("help") -> MessageType.HELP
            else -> MessageType.TEXT
        }
    }
    
    private fun getStatusText(status: com.sewingfactory.data.TaskStatus): String {
        return when (status) {
            com.sewingfactory.data.TaskStatus.CREATED -> "Создано"
            com.sewingfactory.data.TaskStatus.ASSIGNED -> "Назначено"
            com.sewingfactory.data.TaskStatus.IN_PROGRESS -> "В работе"
            com.sewingfactory.data.TaskStatus.COMPLETED -> "Завершено"
            com.sewingfactory.data.TaskStatus.CANCELLED -> "Отменено"
        }
    }
    
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm, dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    
    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm, dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class ChatContext(
    val currentWorker: Worker?,
    val currentTask: WorkTask?,
    val workerStats: WorkerDayStats?
)