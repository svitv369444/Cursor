package com.sewingfactory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "work_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["workerId"]),
        Index(value = ["status"])
    ]
)
@Parcelize
data class WorkTask(
    @PrimaryKey
    val id: String, // QR код из 1С
    val productId: String, // ID изделия
    val workerId: String? = null, // ID работника (null если не назначено)
    val quantity: Int, // Количество изделий в задании
    val completedQuantity: Int = 0, // Выполненное количество
    val status: TaskStatus = TaskStatus.CREATED, // Статус задания
    val priority: Int = 0, // Приоритет (0 - обычный, 1 - высокий, -1 - низкий)
    val deadline: Long? = null, // Срок выполнения
    val startedAt: Long? = null, // Время начала работы
    val completedAt: Long? = null, // Время завершения
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "" // Заметки
) : Parcelable

enum class TaskStatus {
    CREATED,    // Создано
    ASSIGNED,   // Назначено
    IN_PROGRESS, // В работе
    COMPLETED,  // Завершено
    CANCELLED   // Отменено
}