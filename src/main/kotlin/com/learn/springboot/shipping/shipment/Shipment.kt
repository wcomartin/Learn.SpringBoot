package com.learn.springboot.shipping.shipment

import com.learn.springboot.shipping.carrier.Carrier
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "shipments")
data class Shipment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    var shipmentNumber: String = "",

    var salesOrderId: Long = 0,  // reference to sales context
    var warehouseId: Long = 0,   // reference to inventory context

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    var carrier: Carrier? = null,

    var trackingNumber: String? = null,

    @Enumerated(EnumType.STRING)
    var status: ShipmentStatus = ShipmentStatus.PENDING,

    var shippedAt: Instant? = null,
    var deliveredAt: Instant? = null,
    var estimatedDelivery: LocalDate? = null,

    val createdAt: Instant = Instant.now()
) {
    val trackingUrl: String?
        get() = if (trackingNumber != null && carrier?.trackingUrlTemplate != null)
            carrier!!.trackingUrlTemplate!!.replace("{tracking_number}", trackingNumber!!)
        else null
}

enum class ShipmentStatus { PENDING, PICKED, PACKED, SHIPPED, IN_TRANSIT, DELIVERED, RETURNED }
