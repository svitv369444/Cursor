package com.sewingfactory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "workers")
@Parcelize
data class Worker(
    @PrimaryKey
    val id: String, // ID из 1С
    val firstName: String,
    val lastName: String,
    val middleName: String = "",
    val position: String = "Швея", // Должность
    val department: String = "", // Отдел/цех
    val skillLevel: Int = 1, // Уровень квалификации (1-5)
    val hourlyRate: Double = 0.0, // Часовая ставка
    val isActive: Boolean = true, // Активен ли работник
    val phone: String = "",
    val email: String = "",
    val hireDate: Long? = null, // Дата приема на работу
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    val fullName: String
        get() = "$lastName $firstName $middleName".trim()
}