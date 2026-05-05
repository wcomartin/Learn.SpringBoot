package com.learn.springboot.shipping.shipment

import com.learn.springboot.shipping.carrier.CarrierRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.LocalDate

data class CreateShipment(
    val salesOrderId: Long,
    val warehouseId: Long,
    val carrierId: Long? = null,
    val estimatedDelivery: LocalDate? = null
)

data class ShipmentUpdate(
    val carrierId: Long? = null,
    val trackingNumber: String? = null,
    val status: ShipmentStatus? = null,
    val estimatedDelivery: LocalDate? = null
)

@RestController
@RequestMapping("/api/shipments")
class ShipmentController(
    private val repo: ShipmentRepository,
    private val carrierRepo: CarrierRepository
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) salesOrderId: Long?,
        @RequestParam(required = false) status: ShipmentStatus?
    ): List<ShipmentResponse> {
        val results = when {
            salesOrderId != null -> repo.findBySalesOrderId(salesOrderId)
            status != null -> repo.findByStatus(status)
            else -> repo.findAll()
        }
        return results.map { ShipmentResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ShipmentResponse {
        val shipment = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return ShipmentResponse.from(shipment)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: CreateShipment): ShipmentResponse {
        val shipment = Shipment(
            shipmentNumber = "SHP-${System.currentTimeMillis()}",
            salesOrderId = body.salesOrderId,
            warehouseId = body.warehouseId,
            estimatedDelivery = body.estimatedDelivery
        )
        body.carrierId?.let {
            shipment.carrier = carrierRepo.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Carrier not found") }
        }
        return ShipmentResponse.from(repo.save(shipment))
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody patch: ShipmentUpdate): ShipmentResponse {
        val shipment = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        patch.carrierId?.let {
            shipment.carrier = carrierRepo.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Carrier not found") }
        }
        patch.trackingNumber?.let { shipment.trackingNumber = it }
        patch.estimatedDelivery?.let { shipment.estimatedDelivery = it }
        patch.status?.let { newStatus ->
            shipment.status = newStatus
            if (newStatus == ShipmentStatus.SHIPPED) shipment.shippedAt = Instant.now()
            if (newStatus == ShipmentStatus.DELIVERED) shipment.deliveredAt = Instant.now()
        }
        return ShipmentResponse.from(repo.save(shipment))
    }
}
