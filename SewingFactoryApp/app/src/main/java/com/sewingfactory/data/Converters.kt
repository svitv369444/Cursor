package com.sewingfactory.data

import androidx.room.TypeConverter

class Converters {
    
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus {
        return TaskStatus.valueOf(status)
    }
}