package com.learn.springboot.purchasing.purchaseorder

import com.learn.springboot.purchasing.supplier.SupplierRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate

data class CreatePurchaseOrder(
    val supplierId: Long,
    val warehouseId: Long,
    val expectedDelivery: LocalDate? = null,
    val lineItems: List<CreatePOLine> = emptyList()
)

data class CreatePOLine(
    val productId: Long,
    val productSku: String,
    val productName: String,
    val quantity: Int,
    val unitCost: BigDecimal
)

@RestController
@RequestMapping("/api/purchasing/orders")
class PurchaseOrderController(
    private val repo: PurchaseOrderRepository,
    private val supplierRepo: SupplierRepository
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) supplierId: Long?,
        @RequestParam(required = false) status: PurchaseOrderStatus?,
        @RequestParam(required = false) productId: Long?
    ): List<PurchaseOrderResponse> {
        val results = when {
            supplierId != null -> repo.findBySupplierId(supplierId)
            productId != null -> repo.findByProductId(productId)
            status != null -> repo.findByStatus(status)
            else -> repo.findAll()
        }
        return results.map { PurchaseOrderResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): PurchaseOrderResponse {
        val po = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return PurchaseOrderResponse.from(po)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: CreatePurchaseOrder): PurchaseOrderResponse {
        val supplier = supplierRepo.findById(body.supplierId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found") }

        val po = PurchaseOrder(
            poNumber = "PO-${System.currentTimeMillis()}",
            supplier = supplier,
            warehouseId = body.warehouseId,
            expectedDelivery = body.expectedDelivery
        )
        body.lineItems.forEach { line ->
            po.lineItems.add(PurchaseOrderLine(
                order = po,
                productId = line.productId,
                productSku = line.productSku,
                productName = line.productName,
                quantity = line.quantity,
                unitCost = line.unitCost
            ))
        }
        return PurchaseOrderResponse.from(repo.save(po))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Long, @RequestBody body: Map<String, String>): PurchaseOrderResponse {
        val po = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        po.status = PurchaseOrderStatus.valueOf(body["status"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST))
        return PurchaseOrderResponse.from(repo.save(po))
    }
}
