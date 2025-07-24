package com.sewingfactory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sewingfactory.data.WorkTask
import com.sewingfactory.data.TaskStatus
import com.sewingfactory.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (WorkTask) -> Unit
) : ListAdapter<WorkTask, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        
        fun bind(task: WorkTask) {
            binding.apply {
                tvTaskId.text = "Задание: ${task.id}"
                tvTaskQuantity.text = "Количество: ${task.completedQuantity}/${task.quantity}"
                tvTaskStatus.text = getStatusText(task.status)
                tvTaskCreated.text = "Создано: ${dateFormat.format(Date(task.createdAt))}"
                
                // Устанавливаем цвет статуса
                val statusColor = when (task.status) {
                    TaskStatus.CREATED -> android.graphics.Color.GRAY
                    TaskStatus.ASSIGNED -> android.graphics.Color.BLUE
                    TaskStatus.IN_PROGRESS -> android.graphics.Color.YELLOW
                    TaskStatus.COMPLETED -> android.graphics.Color.GREEN
                    TaskStatus.CANCELLED -> android.graphics.Color.RED
                }
                tvTaskStatus.setTextColor(statusColor)
                
                // Показываем прогресс
                progressTask.max = task.quantity
                progressTask.progress = task.completedQuantity
                
                root.setOnClickListener {
                    onTaskClick(task)
                }
            }
        }
        
        private fun getStatusText(status: TaskStatus): String {
            return when (status) {
                TaskStatus.CREATED -> "Создано"
                TaskStatus.ASSIGNED -> "Назначено"
                TaskStatus.IN_PROGRESS -> "В работе"
                TaskStatus.COMPLETED -> "Завершено"
                TaskStatus.CANCELLED -> "Отменено"
            }
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<WorkTask>() {
    override fun areItemsTheSame(oldItem: WorkTask, newItem: WorkTask): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: WorkTask, newItem: WorkTask): Boolean {
        return oldItem == newItem
    }
}