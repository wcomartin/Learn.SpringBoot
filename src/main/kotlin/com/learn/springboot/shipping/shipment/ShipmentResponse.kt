package com.learn.springboot.shipping.shipment

import java.time.Instant
import java.time.LocalDate

data class ShipmentResponse(
    val id: Long,
    val shipmentNumber: String,
    val salesOrderId: Long,
    val warehouseId: Long,
    val carrierId: Long?,
    val carrierName: String?,
    val trackingNumber: String?,
    val trackingUrl: String?,
    val status: ShipmentStatus,
    val shippedAt: Instant?,
    val deliveredAt: Instant?,
    val estimatedDelivery: LocalDate?,
    val createdAt: Instant
) {
    companion object {
        fun from(shipment: Shipment) = ShipmentResponse(
            id = shipment.id,
            shipmentNumber = shipment.shipmentNumber,
            salesOrderId = shipment.salesOrderId,
            warehouseId = shipment.warehouseId,
            carrierId = shipment.carrier?.id,
            carrierName = shipment.carrier?.name,
            trackingNumber = shipment.trackingNumber,
            trackingUrl = shipment.trackingUrl,
            status = shipment.status,
            shippedAt = shipment.shippedAt,
            deliveredAt = shipment.deliveredAt,
            estimatedDelivery = shipment.estimatedDelivery,
            createdAt = shipment.createdAt
        )
    }
}
