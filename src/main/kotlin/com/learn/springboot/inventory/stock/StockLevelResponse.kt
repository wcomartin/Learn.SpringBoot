package com.learn.springboot.inventory.stock

import java.time.Instant

data class StockLevelResponse(
    val id: Long,
    val productId: Long,
    val productSku: String,
    val productName: String,
    val warehouseId: Long,
    val warehouseName: String,
    val quantity: Int,
    val reorderPoint: Int,
    val lowStock: Boolean,
    val updatedAt: Instant
) {
    companion object {
        fun from(stock: StockLevel) = StockLevelResponse(
            id = stock.id,
            productId = stock.product!!.id,
            productSku = stock.product!!.sku,
            productName = stock.product!!.name,
            warehouseId = stock.warehouse!!.id,
            warehouseName = stock.warehouse!!.name,
            quantity = stock.quantity,
            reorderPoint = stock.product!!.reorderPoint,
            lowStock = stock.quantity <= stock.product!!.reorderPoint,
            updatedAt = stock.updatedAt
        )
    }
}
