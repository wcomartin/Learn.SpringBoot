package com.learn.springboot.sales.order

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class SalesOrderLineResponse(
    val id: Long,
    val productId: Long,
    val productSku: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal
) {
    companion object {
        fun from(line: SalesOrderLine) = SalesOrderLineResponse(
            id = line.id,
            productId = line.productId,
            productSku = line.productSku,
            productName = line.productName,
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            lineTotal = line.lineTotal
        )
    }
}

data class SalesOrderResponse(
    val id: Long,
    val orderNumber: String,
    val customerId: Long,
    val status: SalesOrderStatus,
    val orderDate: LocalDate,
    val dueDate: LocalDate?,
    val lineItems: List<SalesOrderLineResponse>,
    val total: BigDecimal,
    val createdAt: Instant
) {
    companion object {
        fun from(order: SalesOrder) = SalesOrderResponse(
            id = order.id,
            orderNumber = order.orderNumber,
            customerId = order.customerId,
            status = order.status,
            orderDate = order.orderDate,
            dueDate = order.dueDate,
            lineItems = order.lineItems.map { SalesOrderLineResponse.from(it) },
            total = order.total,
            createdAt = order.createdAt
        )
    }
}
