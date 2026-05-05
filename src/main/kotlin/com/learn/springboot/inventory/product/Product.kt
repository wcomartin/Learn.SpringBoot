package com.learn.springboot.inventory.product

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "products")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    @Column(unique = true)
    var sku: String = "",

    @field:NotBlank
    var name: String = "",

    var description: String? = null,

    @Column(precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 10, scale = 2)
    var costPrice: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    var status: ProductStatus = ProductStatus.ACTIVE,

    var reorderPoint: Int = 0,

    val createdAt: Instant = Instant.now()
)

enum class ProductStatus { ACTIVE, DISCONTINUED, OUT_OF_STOCK }
