package com.learn.springboot.inventory.stock

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

data class StockAdjustment(val productId: Long, val warehouseId: Long, val quantity: Int)

@RestController
@RequestMapping("/api/stock")
class StockController(
    private val repo: StockLevelRepository,
    private val productRepo: com.learn.springboot.inventory.product.ProductRepository,
    private val warehouseRepo: com.learn.springboot.inventory.warehouse.WarehouseRepository
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) productId: Long?,
        @RequestParam(required = false) warehouseId: Long?
    ): List<StockLevelResponse> {
        val results = when {
            productId != null -> repo.findByProductId(productId)
            warehouseId != null -> repo.findByWarehouseId(warehouseId)
            else -> repo.findAll()
        }
        return results.map { StockLevelResponse.from(it) }
    }

    @GetMapping("/low")
    fun lowStock() = repo.findLowStock().map { StockLevelResponse.from(it) }

    @PostMapping("/adjust")
    fun adjust(@RequestBody adj: StockAdjustment): StockLevelResponse {
        val stock = repo.findByProductIdAndWarehouseId(adj.productId, adj.warehouseId)
            ?: StockLevel(
                product = productRepo.findById(adj.productId)
                    .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found") },
                warehouse = warehouseRepo.findById(adj.warehouseId)
                    .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found") }
            )
        stock.quantity += adj.quantity
        stock.updatedAt = Instant.now()
        return StockLevelResponse.from(repo.save(stock))
    }
}
