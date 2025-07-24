package com.sewingfactory.api

import com.sewingfactory.data.Product
import com.sewingfactory.data.Worker
import com.sewingfactory.data.WorkTask
import retrofit2.Response
import retrofit2.http.*

interface OneCApiService {
    
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<Product>
    
    @GET("workers")
    suspend fun getWorkers(): Response<List<Worker>>
    
    @GET("workers/{id}")
    suspend fun getWorker(@Path("id") id: String): Response<Worker>
    
    @GET("tasks")
    suspend fun getTasks(): Response<List<WorkTask>>
    
    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): Response<WorkTask>
    
    @POST("tasks")
    suspend fun createTask(@Body task: WorkTask): Response<WorkTask>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: WorkTask): Response<WorkTask>
    
    @POST("tasks/{id}/complete")
    suspend fun completeTask(
        @Path("id") id: String,
        @Body completion: TaskCompletion
    ): Response<ApiResponse>
    
    @POST("sync/products")
    suspend fun syncProducts(@Body products: List<Product>): Response<ApiResponse>
    
    @POST("sync/workers")
    suspend fun syncWorkers(@Body workers: List<Worker>): Response<ApiResponse>
    
    @POST("sync/tasks")
    suspend fun syncTasks(@Body tasks: List<WorkTask>): Response<ApiResponse>
}

data class TaskCompletion(
    val completedQuantity: Int,
    val completedAt: Long,
    val workerId: String,
    val notes: String = ""
)

data class ApiResponse(
    val success: Boolean,
    val message: String = "",
    val data: Any? = null
)