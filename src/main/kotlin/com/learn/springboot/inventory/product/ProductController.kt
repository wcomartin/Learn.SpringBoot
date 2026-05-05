package com.learn.springboot.inventory.product

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class ProductPatch(
    val sku: String? = null,
    val name: String? = null,
    val description: String? = null,
    val unitPrice: java.math.BigDecimal? = null,
    val costPrice: java.math.BigDecimal? = null,
    val status: ProductStatus? = null,
    val reorderPoint: Int? = null
)

@RestController
@RequestMapping("/api/products")
class ProductController(private val repo: ProductRepository) {

    @GetMapping
    fun list(@RequestParam(required = false) status: ProductStatus?): List<ProductResponse> =
        (status?.let { repo.findByStatus(it) } ?: repo.findAll())
            .map { ProductResponse.from(it) }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ProductResponse {
        val product = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return ProductResponse.from(product)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody product: Product) = ProductResponse.from(repo.save(product))

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody patch: ProductPatch): ProductResponse {
        val product = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        patch.sku?.let { product.sku = it }
        patch.name?.let { product.name = it }
        patch.description?.let { product.description = it }
        patch.unitPrice?.let { product.unitPrice = it }
        patch.costPrice?.let { product.costPrice = it }
        patch.status?.let { product.status = it }
        patch.reorderPoint?.let { product.reorderPoint = it }
        return ProductResponse.from(repo.save(product))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = repo.deleteById(id)
}
