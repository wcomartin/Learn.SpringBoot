package com.learn.springboot.purchasing.purchaseorder

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PurchaseOrderRepository : JpaRepository<PurchaseOrder, Long> {
    fun findBySupplierId(supplierId: Long): List<PurchaseOrder>
    fun findByStatus(status: PurchaseOrderStatus): List<PurchaseOrder>

    @Query("SELECT DISTINCT o FROM PurchaseOrder o JOIN o.lineItems l WHERE l.productId = :productId")
    fun findByProductId(productId: Long): List<PurchaseOrder>
}
