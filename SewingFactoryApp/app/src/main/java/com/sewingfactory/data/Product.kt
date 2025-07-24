package com.sewingfactory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "products")
@Parcelize
data class Product(
    @PrimaryKey
    val id: String, // ID из 1С
    val name: String, // Название изделия
    val price: Double, // Стоимость за единицу
    val description: String = "", // Описание
    val category: String = "", // Категория
    val complexity: Int = 1, // Сложность изделия (1-5)
    val estimatedTimeMinutes: Int = 0, // Примерное время изготовления в минутах
    val isActive: Boolean = true, // Активно ли изделие
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable