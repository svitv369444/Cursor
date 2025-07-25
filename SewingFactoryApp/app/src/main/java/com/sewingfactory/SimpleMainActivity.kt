package com.sewingfactory

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.sewingfactory.databinding.ActivityMainBinding

class SimpleMainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var currentWorkerName = "Не выбран"
    private var currentTaskId = ""
    private var completedToday = 0
    private var earnedToday = 0.0
    
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleQRCode(result.contents)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        updateUI()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка выбора работника
            btnSelectWorker.setOnClickListener {
                showWorkerSelectionDialog()
            }
            
            // Кнопка сканирования QR кода
            btnScanQr.setOnClickListener {
                if (currentWorkerName == "Не выбран") {
                    Toast.makeText(this@SimpleMainActivity, "Сначала выберите работника", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
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
                if (currentTaskId.isNotEmpty()) {
                    Toast.makeText(this@SimpleMainActivity, "Работа над заданием $currentTaskId начата!", Toast.LENGTH_SHORT).show()
                    btnStartTask.isEnabled = false
                    btnCompleteTask.isEnabled = true
                }
            }
            
            // Кнопка завершения задания
            btnCompleteTask.setOnClickListener {
                showCompleteTaskDialog()
            }
            
            // Кнопка синхронизации
            btnSync.setOnClickListener {
                Toast.makeText(this@SimpleMainActivity, "Синхронизация с 1С (демо)", Toast.LENGTH_SHORT).show()
            }
            
            // Кнопка просмотра статистики
            btnViewStats.setOnClickListener {
                showStatsDialog()
            }
        }
    }
    
    private fun updateUI() {
        binding.apply {
            tvCurrentWorker.text = "Работник: $currentWorkerName"
            
            if (currentTaskId.isNotEmpty()) {
                cardCurrentTask.visibility = android.view.View.VISIBLE
                tvTaskId.text = "Задание: $currentTaskId"
                tvTaskQuantity.text = "Количество: 10" // Демо данные
                tvTaskCompleted.text = "Выполнено: 0"
                tvTaskStatus.text = "Статус: Назначено"
            } else {
                cardCurrentTask.visibility = android.view.View.GONE
            }
            
            tvTodayQuantity.text = "Выполнено сегодня: $completedToday"
            tvTodayEarnings.text = "Заработано: ${String.format("%.2f", earnedToday)} руб."
            tvTodayTime.text = "Время работы: 0 мин."
            
            // Состояние кнопок
            btnScanQr.isEnabled = currentWorkerName != "Не выбран" && currentTaskId.isEmpty()
            btnStartTask.isEnabled = currentTaskId.isNotEmpty()
            btnCompleteTask.isEnabled = false
        }
    }
    
    private fun showWorkerSelectionDialog() {
        val workers = arrayOf(
            "Иванова Мария Петровна",
            "Петрова Анна Сергеевна", 
            "Сидорова Елена Александровна",
            "Козлова Ольга Викторовна"
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Выберите работника")
            .setItems(workers) { _, which ->
                currentWorkerName = workers[which]
                updateUI()
            }
            .show()
    }
    
    private fun handleQRCode(qrContent: String) {
        currentTaskId = qrContent
        Toast.makeText(this, "Задание $qrContent назначено", Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    private fun showCompleteTaskDialog() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Введите количество выполненных изделий"
            setText("10")
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Завершение задания")
            .setMessage("Задание: $currentTaskId\nТребуется: 10\nВыполнено: 0")
            .setView(input)
            .setPositiveButton("Завершить") { _, _ ->
                val quantity = input.text.toString().toIntOrNull() ?: 0
                if (quantity > 0) {
                    completeTask(quantity)
                } else {
                    Toast.makeText(this, "Введите корректное количество", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun completeTask(quantity: Int) {
        val pricePerItem = 150.0 // Демо цена
        val earned = quantity * pricePerItem
        
        completedToday += quantity
        earnedToday += earned
        
        Toast.makeText(this, "Задание завершено! Заработано: ${String.format("%.2f", earned)} руб.", Toast.LENGTH_LONG).show()
        
        // Сброс текущего задания
        currentTaskId = ""
        updateUI()
    }
    
    private fun showStatsDialog() {
        val statsText = """
            Статистика работника: $currentWorkerName
            Дата: ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date())}
            
            Выполнено изделий: $completedToday
            Заработано: ${String.format("%.2f", earnedToday)} руб.
            
            Это демо версия приложения.
            Полная версия включает:
            - Интеграцию с 1С
            - База данных заданий
            - Детальная статистика
            - Учет времени работы
        """.trimIndent()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Статистика")
            .setMessage(statsText)
            .setPositiveButton("OK", null)
            .show()
    }
}