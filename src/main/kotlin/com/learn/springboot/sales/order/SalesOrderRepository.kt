package com.learn.springboot.sales.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SalesOrderRepository : JpaRepository<SalesOrder, Long> {
    fun findByCustomerId(customerId: Long): List<SalesOrder>
    fun findByStatus(status: SalesOrderStatus): List<SalesOrder>
    fun findByOrderNumber(orderNumber: String): SalesOrder?

    @Query("SELECT DISTINCT o FROM SalesOrder o JOIN o.lineItems l WHERE l.productId = :productId")
    fun findByProductId(productId: Long): List<SalesOrder>
}
