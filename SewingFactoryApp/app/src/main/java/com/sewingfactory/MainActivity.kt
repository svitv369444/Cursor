package com.sewingfactory

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.sewingfactory.adapter.WorkerAdapter
import com.sewingfactory.adapter.TaskAdapter
import com.sewingfactory.databinding.ActivityMainBinding
import com.sewingfactory.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var workerAdapter: WorkerAdapter
    private lateinit var taskAdapter: TaskAdapter
    
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.scanQRCode(result.contents)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setupUI()
        setupObservers()
        setupRecyclerViews()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка сканирования QR кода
            btnScanQr.setOnClickListener {
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Отсканируйте QR код задания")
                    setCameraId(0)
                    setBeepEnabled(true)
                    setBarcodeImageEnabled(true)
                }
                barcodeLauncher.launch(options)
            }
            
            // Кнопка начала работы
            btnStartTask.setOnClickListener {
                viewModel.startTask()
            }
            
            // Кнопка завершения задания
            btnCompleteTask.setOnClickListener {
                showCompleteTaskDialog()
            }
            
            // Кнопка синхронизации
            btnSync.setOnClickListener {
                viewModel.syncWithOneC()
            }
            
            // Кнопка выбора работника
            btnSelectWorker.setOnClickListener {
                showWorkerSelectionDialog()
            }
            
            // Кнопка просмотра статистики
            btnViewStats.setOnClickListener {
                showStatsDialog()
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.currentWorker.observe(this) { worker ->
            binding.tvCurrentWorker.text = if (worker != null) {
                "Работник: ${worker.fullName}"
            } else {
                "Работник не выбран"
            }
            updateUIState()
        }
        
        viewModel.currentTask.observe(this) { task ->
            if (task != null) {
                binding.apply {
                    cardCurrentTask.visibility = android.view.View.VISIBLE
                    tvTaskId.text = "Задание: ${task.id}"
                    tvTaskQuantity.text = "Количество: ${task.quantity}"
                    tvTaskCompleted.text = "Выполнено: ${task.completedQuantity}"
                    tvTaskStatus.text = "Статус: ${getStatusText(task.status)}"
                }
            } else {
                binding.cardCurrentTask.visibility = android.view.View.GONE
            }
            updateUIState()
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
        
        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.workerStats.observe(this) { stats ->
            if (stats != null) {
                binding.apply {
                    tvTodayQuantity.text = "Выполнено сегодня: ${stats.totalQuantity}"
                    tvTodayEarnings.text = "Заработано: ${String.format("%.2f", stats.totalEarnings)} руб."
                    tvTodayTime.text = "Время работы: ${stats.totalWorkTimeMinutes} мин."
                }
            }
        }
    }
    
    private fun setupRecyclerViews() {
        workerAdapter = WorkerAdapter { worker ->
            viewModel.setCurrentWorker(worker)
        }
        
        taskAdapter = TaskAdapter { task ->
            // Обработка клика по заданию
        }
    }
    
    private fun updateUIState() {
        val hasWorker = viewModel.currentWorker.value != null
        val hasTask = viewModel.currentTask.value != null
        val taskInProgress = viewModel.currentTask.value?.status == com.sewingfactory.data.TaskStatus.IN_PROGRESS
        
        binding.apply {
            btnScanQr.isEnabled = hasWorker && !hasTask
            btnStartTask.isEnabled = hasTask && !taskInProgress
            btnCompleteTask.isEnabled = hasTask && taskInProgress
        }
    }
    
    private fun showWorkerSelectionDialog() {
        viewModel.allWorkers.observe(this) { workers ->
            if (workers.isNotEmpty()) {
                val workerNames = workers.map { it.fullName }.toTypedArray()
                MaterialAlertDialogBuilder(this)
                    .setTitle("Выберите работника")
                    .setItems(workerNames) { _, which ->
                        viewModel.setCurrentWorker(workers[which])
                    }
                    .show()
            } else {
                Toast.makeText(this, "Список работников пуст. Выполните синхронизацию с 1С", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showCompleteTaskDialog() {
        val task = viewModel.currentTask.value ?: return
        
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Введите количество выполненных изделий"
            setText(task.quantity.toString())
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Завершение задания")
            .setMessage("Задание: ${task.id}\nТребуется: ${task.quantity}\nВыполнено: ${task.completedQuantity}")
            .setView(input)
            .setPositiveButton("Завершить") { _, _ ->
                val quantity = input.text.toString().toIntOrNull()
                if (quantity != null && quantity > 0) {
                    viewModel.completeTask(quantity)
                } else {
                    Toast.makeText(this, "Введите корректное количество", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun showStatsDialog() {
        val worker = viewModel.currentWorker.value ?: return
        val stats = viewModel.workerStats.value ?: return
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val statsText = """
            Статистика работника: ${worker.fullName}
            Дата: ${dateFormat.format(Date(stats.date))}
            
            Выполнено изделий: ${stats.totalQuantity}
            Завершено заданий: ${stats.completedTasksCount}
            Время работы: ${stats.totalWorkTimeMinutes} мин.
            Заработано: ${String.format("%.2f", stats.totalEarnings)} руб.
        """.trimIndent()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Статистика")
            .setMessage(statsText)
            .setPositiveButton("OK", null)
            .show()
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
}