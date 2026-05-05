package com.learn.springboot.purchasing.purchaseorder

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class PurchaseOrderLineResponse(
    val id: Long,
    val productId: Long,
    val productSku: String,
    val productName: String,
    val quantity: Int,
    val receivedQuantity: Int,
    val unitCost: BigDecimal,
    val lineTotal: BigDecimal,
    val fullyReceived: Boolean
) {
    companion object {
        fun from(line: PurchaseOrderLine) = PurchaseOrderLineResponse(
            id = line.id,
            productId = line.productId,
            productSku = line.productSku,
            productName = line.productName,
            quantity = line.quantity,
            receivedQuantity = line.receivedQuantity,
            unitCost = line.unitCost,
            lineTotal = line.lineTotal,
            fullyReceived = line.fullyReceived
        )
    }
}

data class PurchaseOrderResponse(
    val id: Long,
    val poNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val status: PurchaseOrderStatus,
    val orderDate: LocalDate,
    val expectedDelivery: LocalDate?,
    val warehouseId: Long,
    val lineItems: List<PurchaseOrderLineResponse>,
    val total: BigDecimal,
    val createdAt: Instant
) {
    companion object {
        fun from(po: PurchaseOrder) = PurchaseOrderResponse(
            id = po.id,
            poNumber = po.poNumber,
            supplierId = po.supplier!!.id,
            supplierName = po.supplier!!.name,
            status = po.status,
            orderDate = po.orderDate,
            expectedDelivery = po.expectedDelivery,
            warehouseId = po.warehouseId,
            lineItems = po.lineItems.map { PurchaseOrderLineResponse.from(it) },
            total = po.total,
            createdAt = po.createdAt
        )
    }
}
