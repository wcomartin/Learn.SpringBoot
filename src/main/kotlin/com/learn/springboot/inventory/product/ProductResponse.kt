package com.learn.springboot.inventory.product

import java.math.BigDecimal
import java.time.Instant

data class ProductResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val unitPrice: BigDecimal,
    val costPrice: BigDecimal,
    val status: ProductStatus,
    val reorderPoint: Int,
    val createdAt: Instant
) {
    companion object {
        fun from(product: Product) = ProductResponse(
            id = product.id,
            sku = product.sku,
            name = product.name,
            description = product.description,
            unitPrice = product.unitPrice,
            costPrice = product.costPrice,
            status = product.status,
            reorderPoint = product.reorderPoint,
            createdAt = product.createdAt
        )
    }
}
