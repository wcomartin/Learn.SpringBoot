package com.learn.springboot.inventory.stock

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StockLevelRepository : JpaRepository<StockLevel, Long> {
    fun findByProductId(productId: Long): List<StockLevel>
    fun findByWarehouseId(warehouseId: Long): List<StockLevel>
    fun findByProductIdAndWarehouseId(productId: Long, warehouseId: Long): StockLevel?

    @Query("SELECT s FROM StockLevel s WHERE s.quantity <= s.product.reorderPoint")
    fun findLowStock(): List<StockLevel>
}
