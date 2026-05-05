package com.learn.springboot.inventory.stock

import com.learn.springboot.inventory.product.Product
import com.learn.springboot.inventory.warehouse.Warehouse
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "stock_levels",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_id", "warehouse_id"])]
)
data class StockLevel(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: Warehouse? = null,

    var quantity: Int = 0,

    var updatedAt: Instant = Instant.now()
)
