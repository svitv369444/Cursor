package com.sewingfactory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "work_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkTask::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class WorkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: String, // ID задания
    val workerId: String, // ID работника
    val startTime: Long, // Время начала сессии
    val endTime: Long? = null, // Время окончания сессии (null если активна)
    val completedQuantity: Int = 0, // Количество выполненных изделий за сессию
    val notes: String = "", // Заметки к сессии
    val isActive: Boolean = true, // Активна ли сессия
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    val durationMinutes: Long?
        get() = endTime?.let { (it - startTime) / (1000 * 60) }
    
    val isCompleted: Boolean
        get() = endTime != null
}