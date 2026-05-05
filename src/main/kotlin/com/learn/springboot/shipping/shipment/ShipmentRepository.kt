package com.learn.springboot.shipping.shipment

import org.springframework.data.jpa.repository.JpaRepository

interface ShipmentRepository : JpaRepository<Shipment, Long> {
    fun findBySalesOrderId(salesOrderId: Long): List<Shipment>
    fun findByStatus(status: ShipmentStatus): List<Shipment>
    fun findByTrackingNumber(trackingNumber: String): Shipment?
}
