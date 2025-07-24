package com.sewingfactory.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.sewingfactory.data.Product

@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name")
    fun getAllActiveProducts(): LiveData<List<Product>>
    
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): LiveData<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?
    
    @Query("SELECT * FROM products WHERE name LIKE :searchQuery AND isActive = 1")
    suspend fun searchProducts(searchQuery: String): List<Product>
    
    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1")
    suspend fun getProductsByCategory(category: String): List<Product>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Delete
    suspend fun deleteProduct(product: Product)
    
    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun deactivateProduct(id: String)
    
    @Query("SELECT DISTINCT category FROM products WHERE isActive = 1 ORDER BY category")
    suspend fun getAllCategories(): List<String>
}