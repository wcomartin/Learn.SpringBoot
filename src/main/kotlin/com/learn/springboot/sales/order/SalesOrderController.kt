package com.learn.springboot.sales.order

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate

data class CreateSalesOrder(
    val customerId: Long,
    val dueDate: LocalDate? = null,
    val lineItems: List<CreateLineItem> = emptyList()
)

data class CreateLineItem(
    val productId: Long,
    val productSku: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)

@RestController
@RequestMapping("/api/sales/orders")
class SalesOrderController(private val repo: SalesOrderRepository) {

    @GetMapping
    fun list(
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) status: SalesOrderStatus?,
        @RequestParam(required = false) productId: Long?
    ): List<SalesOrderResponse> {
        val results = when {
            customerId != null -> repo.findByCustomerId(customerId)
            productId != null -> repo.findByProductId(productId)
            status != null -> repo.findByStatus(status)
            else -> repo.findAll()
        }
        return results.map { SalesOrderResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): SalesOrderResponse {
        val order = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return SalesOrderResponse.from(order)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: CreateSalesOrder): SalesOrderResponse {
        val order = SalesOrder(
            orderNumber = "SO-${System.currentTimeMillis()}",
            customerId = body.customerId,
            dueDate = body.dueDate
        )
        body.lineItems.forEach { line ->
            order.lineItems.add(SalesOrderLine(
                order = order,
                productId = line.productId,
                productSku = line.productSku,
                productName = line.productName,
                quantity = line.quantity,
                unitPrice = line.unitPrice
            ))
        }
        return SalesOrderResponse.from(repo.save(order))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Long, @RequestBody body: Map<String, String>): SalesOrderResponse {
        val order = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val newStatus = SalesOrderStatus.valueOf(body["status"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST))
        order.status = newStatus
        return SalesOrderResponse.from(repo.save(order))
    }
}
