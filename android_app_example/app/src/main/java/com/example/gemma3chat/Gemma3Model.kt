package com.example.gemma3chat

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Класс для работы с моделью Gemma 3
 * Обеспечивает загрузку модели и выполнение инференса
 */
class Gemma3Model(private val context: Context) {
    
    companion object {
        private const val TAG = "Gemma3Model"
        private const val MODEL_FILENAME = "gemma_3_2b.tflite"
        private const val MAX_SEQUENCE_LENGTH = 512
        private const val VOCAB_SIZE = 256000 // Примерный размер словаря Gemma
    }
    
    private var interpreter: Interpreter? = null
    private var modelLoaded = false
    
    /**
     * Загрузка модели из assets или внутреннего хранилища
     */
    suspend fun loadModel(): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelBuffer = loadModelFile()
            
            // Настройки для оптимизации
            val options = Interpreter.Options().apply {
                // Включаем GPU если доступно
                setUseNNAPI(true)
                setNumThreads(4)
            }
            
            interpreter = Interpreter(modelBuffer, options)
            modelLoaded = true
            
            Log.i(TAG, "Модель Gemma 3 успешно загружена")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки модели: ${e.message}", e)
            false
        }
    }
    
    /**
     * Загрузка файла модели
     */
    private fun loadModelFile(): ByteBuffer {
        return try {
            // Попытка загрузить из assets
            context.assets.openFd(MODEL_FILENAME).use { fileDescriptor ->
                FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                    val fileChannel = inputStream.channel
                    val startOffset = fileDescriptor.startOffset
                    val declaredLength = fileDescriptor.declaredLength
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Модель не найдена в assets, пытаемся загрузить из внутреннего хранилища")
            
            // Попытка загрузить из внутреннего хранилища
            val modelFile = context.filesDir.resolve(MODEL_FILENAME)
            if (modelFile.exists()) {
                FileInputStream(modelFile).use { inputStream ->
                    val fileChannel = inputStream.channel
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
                }
            } else {
                throw IOException("Файл модели не найден: $MODEL_FILENAME")
            }
        }
    }
    
    /**
     * Генерация ответа на основе входного текста
     */
    suspend fun generateResponse(inputText: String, maxTokens: Int = 100): String = withContext(Dispatchers.Default) {
        if (!modelLoaded || interpreter == null) {
            throw IllegalStateException("Модель не загружена")
        }
        
        try {
            // Токенизация входного текста (упрощенная версия)
            val inputTokens = tokenizeText(inputText)
            
            // Подготовка входного тензора
            val inputBuffer = ByteBuffer.allocateDirect(MAX_SEQUENCE_LENGTH * 4)
                .order(ByteOrder.nativeOrder())
            
            // Заполнение буфера токенами
            for (i in 0 until minOf(inputTokens.size, MAX_SEQUENCE_LENGTH)) {
                inputBuffer.putInt(inputTokens[i])
            }
            
            // Дополнение нулями до MAX_SEQUENCE_LENGTH
            for (i in inputTokens.size until MAX_SEQUENCE_LENGTH) {
                inputBuffer.putInt(0)
            }
            
            inputBuffer.rewind()
            
            // Подготовка выходного тензора
            val outputBuffer = ByteBuffer.allocateDirect(VOCAB_SIZE * 4)
                .order(ByteOrder.nativeOrder())
            
            // Выполнение инференса
            interpreter?.run(inputBuffer, outputBuffer)
            
            // Обработка результата
            outputBuffer.rewind()
            val outputTokens = mutableListOf<Int>()
            
            // Генерация токенов
            for (i in 0 until maxTokens) {
                val probabilities = FloatArray(VOCAB_SIZE)
                for (j in 0 until VOCAB_SIZE) {
                    probabilities[j] = outputBuffer.getFloat(j * 4)
                }
                
                // Выбор следующего токена (простейший способ - argmax)
                val nextToken = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                outputTokens.add(nextToken)
                
                // Проверка на токен окончания
                if (nextToken == 2) break // EOS token
            }
            
            // Детokenization
            detokenizeText(outputTokens)
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка генерации: ${e.message}", e)
            "Ошибка при генерации ответа"
        }
    }
    
    /**
     * Простая токенизация текста (для примера)
     * В реальном приложении используйте SentencePiece или аналогичную библиотеку
     */
    private fun tokenizeText(text: String): IntArray {
        // Упрощенная токенизация - просто конвертируем символы в числа
        val tokens = mutableListOf<Int>()
        tokens.add(1) // BOS token
        
        for (char in text) {
            tokens.add(char.code.coerceIn(0, VOCAB_SIZE - 1))
        }
        
        return tokens.toIntArray()
    }
    
    /**
     * Простая детokенизация (для примера)
     */
    private fun detokenizeText(tokens: List<Int>): String {
        val result = StringBuilder()
        
        for (token in tokens) {
            if (token == 1 || token == 2) continue // Пропускаем BOS/EOS токены
            if (token in 32..126) { // Печатные ASCII символы
                result.append(token.toChar())
            }
        }
        
        return result.toString().takeIf { it.isNotEmpty() } ?: "Сгенерированный ответ"
    }
    
    /**
     * Освобождение ресурсов
     */
    fun cleanup() {
        interpreter?.close()
        interpreter = null
        modelLoaded = false
        Log.i(TAG, "Ресурсы модели освобождены")
    }
    
    /**
     * Проверка доступности GPU
     */
    fun isGpuAvailable(): Boolean {
        return try {
            val options = Interpreter.Options().apply {
                setUseNNAPI(true)
            }
            // Попытка создать временный интерпретатор с GPU
            val testBuffer = ByteBuffer.allocateDirect(4)
            val testInterpreter = Interpreter(testBuffer, options)
            testInterpreter.close()
            true
        } catch (e: Exception) {
            Log.w(TAG, "GPU недоступно: ${e.message}")
            false
        }
    }
    
    /**
     * Получение информации о модели
     */
    fun getModelInfo(): String {
        return if (modelLoaded && interpreter != null) {
            """
            Модель: Gemma 3 2B
            Статус: Загружена
            GPU ускорение: ${if (isGpuAvailable()) "Доступно" else "Недоступно"}
            Максимальная длина: $MAX_SEQUENCE_LENGTH токенов
            Размер словаря: $VOCAB_SIZE токенов
            """.trimIndent()
        } else {
            "Модель не загружена"
        }
    }
}